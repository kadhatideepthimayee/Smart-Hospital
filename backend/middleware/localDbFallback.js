const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');

const dbDir = path.join(__dirname, '../local_db');

if (!fs.existsSync(dbDir)) {
  fs.mkdirSync(dbDir, { recursive: true });
}

function generateId() {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}

function readDataFile(modelName) {
  const filePath = path.join(dbDir, `${modelName.toLowerCase()}.json`);
  if (!fs.existsSync(filePath)) {
    return [];
  }
  try {
    return JSON.parse(fs.readFileSync(filePath, 'utf8'));
  } catch (e) {
    return [];
  }
}

function writeDataFile(modelName, data) {
  const filePath = path.join(dbDir, `${modelName.toLowerCase()}.json`);
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf8');
}

function matchQuery(item, query) {
  if (!query) return true;
  for (let key in query) {
    const val = query[key];
    if (typeof val === 'object' && val !== null) {
      if (key === '$or' && Array.isArray(val)) {
        return val.some(q => matchQuery(item, q));
      }
      const op = Object.keys(val)[0];
      if (op === '$in' && Array.isArray(val[op])) {
        if (!val[op].includes(item[key])) return false;
      } else if (op === '$gte') {
        if (!(item[key] >= val[op])) return false;
      } else if (op === '$lte') {
        if (!(item[key] <= val[op])) return false;
      } else if (op === '$gt') {
        if (!(item[key] > val[op])) return false;
      } else if (op === '$lt') {
        if (!(item[key] < val[op])) return false;
      } else {
        if (item[key] !== val) return false;
      }
    } else {
      if (item[key] !== val) return false;
    }
  }
  return true;
}

function applyUpdate(item, update) {
  if (!update) return;
  const keys = Object.keys(update);
  let hasOperators = false;
  for (let key of keys) {
    if (key.startsWith('$')) {
      hasOperators = true;
      const opVal = update[key];
      if (key === '$set') {
        Object.assign(item, opVal);
      } else if (key === '$push') {
        for (let prop in opVal) {
          if (!Array.isArray(item[prop])) item[prop] = [];
          item[prop].push(opVal[prop]);
        }
      } else if (key === '$pull') {
        for (let prop in opVal) {
          if (Array.isArray(item[prop])) {
            item[prop] = item[prop].filter(v => v !== opVal[prop] && v._id !== opVal[prop]);
          }
        }
      } else if (key === '$inc') {
        for (let prop in opVal) {
          item[prop] = (item[prop] || 0) + opVal[prop];
        }
      }
    }
  }
  if (!hasOperators) {
    Object.assign(item, update);
  }
}

function populateField(item, pathName) {
  if (!item) return;
  if (pathName === 'doctor') {
    const doctorId = item.doctor;
    if (doctorId) {
      const doctorProfiles = readDataFile('DoctorProfile');
      let profile = doctorProfiles.find(p => p._id.toString() === doctorId.toString() || p.uid === doctorId.toString());
      if (!profile) {
        const users = readDataFile('User');
        profile = users.find(u => u._id.toString() === doctorId.toString() && u.role === 'DOCTOR');
      }
      if (profile) {
        item.doctor = profile;
      }
    }
  } else if (pathName === 'patient') {
    const patientId = item.patient;
    if (patientId) {
      const users = readDataFile('User');
      const user = users.find(u => u._id.toString() === patientId.toString());
      if (user) {
        item.patient = user;
      }
    }
  }
}

class MockDocument {
  constructor(modelName, data) {
    Object.assign(this, data);
    if (!this._id) {
      this._id = generateId();
    }
    this.__modelName = modelName;
  }
  
  async save() {
    const list = readDataFile(this.__modelName);
    const index = list.findIndex(item => item._id.toString() === this._id.toString());
    if (index >= 0) {
      list[index] = { ...list[index], ...this };
    } else {
      list.push(this);
    }
    writeDataFile(this.__modelName, list);
    return this;
  }

  async updateOne(update) {
    Object.assign(this, update);
    return this.save();
  }

  async deleteOne() {
    const list = readDataFile(this.__modelName);
    const filtered = list.filter(item => item._id.toString() !== this._id.toString());
    writeDataFile(this.__modelName, filtered);
    return { deletedCount: 1 };
  }
}

class MockQuery {
  constructor(data) {
    this.data = data;
    this.populatePaths = [];
  }
  
  populate(pathName) {
    this.populatePaths.push(pathName);
    return this;
  }
  
  select() { return this; }
  
  sort(sortObj) {
    if (Array.isArray(this.data) && sortObj) {
      const keys = Object.keys(sortObj);
      this.data.sort((a, b) => {
        for (let key of keys) {
          const order = sortObj[key];
          if (a[key] < b[key]) return order === -1 ? 1 : -1;
          if (a[key] > b[key]) return order === -1 ? -1 : 1;
        }
        return 0;
      });
    }
    return this;
  }
  
  limit(n) {
    if (Array.isArray(this.data)) {
      this.data = this.data.slice(0, n);
    }
    return this;
  }
  
  skip(n) {
    if (Array.isArray(this.data)) {
      this.data = this.data.slice(n);
    }
    return this;
  }
  
  exec() {
    let result = this.data;
    if (result) {
      if (Array.isArray(result)) {
        result.forEach(item => {
          this.populatePaths.forEach(p => populateField(item, p));
        });
      } else {
        this.populatePaths.forEach(p => populateField(result, p));
      }
    }
    return Promise.resolve(result);
  }
  
  then(onFulfilled, onRejected) {
    return this.exec().then(onFulfilled, onRejected);
  }
}

// Override mongoose.model compilation
const origModel = mongoose.model;
mongoose.model = function(name, schema) {
  const model = origModel.apply(this, arguments);
  
  // Wrap constructor and static methods
  const wrappedModel = function(doc) {
    if (mongoose.connection.readyState === 1) {
      return new model(doc);
    } else {
      return new MockDocument(name, doc);
    }
  };
  
  Object.setPrototypeOf(wrappedModel, model);
  wrappedModel.prototype = model.prototype;
  
  wrappedModel.find = function(query) {
    if (mongoose.connection.readyState === 1) {
      return model.find.apply(this, arguments);
    }
    const list = readDataFile(name).map(d => new MockDocument(name, d));
    const results = list.filter(item => matchQuery(item, query));
    return new MockQuery(results);
  };
  
  wrappedModel.findOne = function(query) {
    if (mongoose.connection.readyState === 1) {
      return model.findOne.apply(this, arguments);
    }
    const list = readDataFile(name).map(d => new MockDocument(name, d));
    const result = list.find(item => matchQuery(item, query)) || null;
    return new MockQuery(result);
  };
  
  wrappedModel.findById = function(id) {
    if (mongoose.connection.readyState === 1) {
      return model.findById.apply(this, arguments);
    }
    const list = readDataFile(name).map(d => new MockDocument(name, d));
    const result = list.find(item => item._id.toString() === (id && id.toString())) || null;
    return new MockQuery(result);
  };
  
  wrappedModel.findByIdAndUpdate = function(id, update, options) {
    if (mongoose.connection.readyState === 1) {
      return model.findByIdAndUpdate.apply(this, arguments);
    }
    const list = readDataFile(name).map(d => new MockDocument(name, d));
    const index = list.findIndex(item => item._id.toString() === (id && id.toString()));
    if (index >= 0) {
      const current = list[index];
      applyUpdate(current, update);
      writeDataFile(name, list);
      const doc = new MockDocument(name, list[index]);
      return new MockQuery(doc);
    }
    return new MockQuery(null);
  };
  
  wrappedModel.findOneAndUpdate = function(query, update, options) {
    if (mongoose.connection.readyState === 1) {
      return model.findOneAndUpdate.apply(this, arguments);
    }
    const list = readDataFile(name).map(d => new MockDocument(name, d));
    const index = list.findIndex(item => matchQuery(item, query));
    if (index >= 0) {
      const current = list[index];
      applyUpdate(current, update);
      writeDataFile(name, list);
      const doc = new MockDocument(name, list[index]);
      return new MockQuery(doc);
    }
    return new MockQuery(null);
  };
  
  wrappedModel.findByIdAndDelete = function(id) {
    if (mongoose.connection.readyState === 1) {
      return model.findByIdAndDelete.apply(this, arguments);
    }
    const list = readDataFile(name).map(d => new MockDocument(name, d));
    const index = list.findIndex(item => item._id.toString() === (id && id.toString()));
    let result = null;
    if (index >= 0) {
      result = new MockDocument(name, list[index]);
      list.splice(index, 1);
      writeDataFile(name, list);
    }
    return new MockQuery(result);
  };
  
  wrappedModel.findOneAndDelete = function(query) {
    if (mongoose.connection.readyState === 1) {
      return model.findOneAndDelete.apply(this, arguments);
    }
    const list = readDataFile(name).map(d => new MockDocument(name, d));
    const index = list.findIndex(item => matchQuery(item, query));
    let result = null;
    if (index >= 0) {
      result = new MockDocument(name, list[index]);
      list.splice(index, 1);
      writeDataFile(name, list);
    }
    return new MockQuery(result);
  };
  
  wrappedModel.create = async function(doc) {
    if (mongoose.connection.readyState === 1) {
      return model.create.apply(this, arguments);
    }
    const docs = Array.isArray(doc) ? doc : [doc];
    const created = [];
    const list = readDataFile(name);
    for (let d of docs) {
      const mockDoc = new MockDocument(name, d);
      list.push(mockDoc);
      created.push(mockDoc);
    }
    writeDataFile(name, list);
    return Array.isArray(doc) ? created : created[0];
  };
  
  wrappedModel.insertMany = async function(docs) {
    if (mongoose.connection.readyState === 1) {
      return model.insertMany.apply(this, arguments);
    }
    return this.create(docs);
  };
  
  wrappedModel.countDocuments = function(query) {
    if (mongoose.connection.readyState === 1) {
      return model.countDocuments.apply(this, arguments);
    }
    const list = readDataFile(name);
    const count = list.filter(item => matchQuery(item, query)).length;
    return new MockQuery(count);
  };
  
  wrappedModel.distinct = function(field, query) {
    if (mongoose.connection.readyState === 1) {
      return model.distinct.apply(this, arguments);
    }
    const list = readDataFile(name).filter(item => matchQuery(item, query));
    const values = [...new Set(list.map(item => item[field]))];
    return new MockQuery(values);
  };
  
  wrappedModel.updateOne = function(query, update, options) {
    if (mongoose.connection.readyState === 1) {
      return model.updateOne.apply(this, arguments);
    }
    const list = readDataFile(name).map(d => new MockDocument(name, d));
    const index = list.findIndex(item => matchQuery(item, query));
    if (index >= 0) {
      applyUpdate(list[index], update);
      writeDataFile(name, list);
      return new MockQuery({ matchedCount: 1, modifiedCount: 1 });
    }
    return new MockQuery({ matchedCount: 0, modifiedCount: 0 });
  };
  
  wrappedModel.deleteOne = function(query) {
    if (mongoose.connection.readyState === 1) {
      return model.deleteOne.apply(this, arguments);
    }
    const list = readDataFile(name);
    const initialLength = list.length;
    const filtered = list.filter(item => !matchQuery(item, query));
    writeDataFile(name, filtered);
    return new MockQuery({ deletedCount: initialLength - filtered.length });
  };
  
  return wrappedModel;
};

console.log('MongoDB transparent local file fallback active.');
