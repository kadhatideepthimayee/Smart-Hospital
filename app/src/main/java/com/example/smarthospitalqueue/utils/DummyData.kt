package com.example.smarthospitalqueue.utils

object DummyData {
    val doctor = Doctor(
        name = "Dr. Ramesh Iyer",
        specialization = "Neurologist",
        roomNumber = "204"
    )

    val currentPatient = Patient(
        id = "PAT-2024-001",
        name = "Deepthi Krishnan",
        profileInitials = "DK",
        patientId = "HOSP-8821",
        bloodGroup = "B+",
        age = 28,
        gender = "Female",
        phone = "+91 98765 43210",
        email = "deepthi.k@example.com",
        address = "Apt 4B, Skyview Residency, OMR, Chennai",
        emergencyContact = "Ravi (Husband) - 98765 00000",
        avatarInitials = "DK"
    )

    val upcomingAppointment = Appointment(
        doctor = doctor,
        department = "Neurology",
        date = "Today, 28 May",
        time = "10:00 AM",
        estimatedWait = 15,
        consultationFee = 500,
        tokenNumber = "T-47"
    )

    val activeQueue = QueueStatus(
        isActive = true,
        tokenNumber = 47,
        currentServing = 39,
        totalAhead = 8,
        estimatedWaitMinutes = 24,
        department = "Cardiology",
        doctorName = "Dr. Priya Venkatesh",
        roomNumber = "OPD-3B",
        queueProgress = 0.82f,
        status = QueueStatusType.ALMOST_THERE
    )

    val departments = listOf(
        Department("Cardiology", doctorCount = 12, congestionLevel = CongestionLevel.HIGH, avgWait = 25),
        Department("Neurology", doctorCount = 8, congestionLevel = CongestionLevel.LOW, avgWait = 10),
        Department("Pediatrics", doctorCount = 15, congestionLevel = CongestionLevel.MODERATE, avgWait = 18),
        Department("Orthopedics", doctorCount = 10, congestionLevel = CongestionLevel.LOW, avgWait = 12)
    )

    val doctors = listOf(
        doctor,
        Doctor("Dr. Priya Venkatesh", "Cardiologist", "302"),
        Doctor("Dr. Amit Sharma", "Pediatrician", "105")
    )

    val timeSlots = listOf(
        TimeSlot("09:00 AM"),
        TimeSlot("09:30 AM"),
        TimeSlot("10:00 AM"),
        TimeSlot("10:30 AM")
    )

    val medicalRecords = listOf(
        MedicalRecord(
            type = RecordType.PRESCRIPTION,
            date = "24 May 2024",
            doctorName = "Dr. Ramesh Iyer",
            department = "Neurology",
            description = "Prescription for migraines and sleep cycle improvement.",
            fileSize = "1.2 MB"
        ),
        MedicalRecord(
            type = RecordType.LAB_REPORT,
            date = "20 May 2024",
            doctorName = "Dr. Amit Sharma",
            department = "Pathology",
            description = "Full body blood work and vitamin deficiency analysis.",
            fileSize = "2.5 MB"
        ),
        MedicalRecord(
            type = RecordType.CONSULTATION,
            date = "15 May 2024",
            doctorName = "Dr. Priya Venkatesh",
            department = "Cardiology",
            description = "Routine heart checkup and ECG review.",
            fileSize = "450 KB"
        ),
        MedicalRecord(
            type = RecordType.SCAN,
            date = "10 May 2024",
            doctorName = "Dr. Suresh Kumar",
            department = "Radiology",
            description = "MRI scan of the lumbar spine.",
            fileSize = "15.8 MB"
        )
    )

    val notifications = listOf(
        Notification(
            id = "1",
            title = "Queue Update",
            body = "Your turn is coming up in approximately 10 minutes. Please proceed to Room 204.",
            time = "5 mins ago",
            type = NotificationType.QUEUE,
            isRead = false
        ),
        Notification(
            id = "2",
            title = "Payment Successful",
            body = "Your payment of ₹500 for consultation with Dr. Ramesh Iyer was successful.",
            time = "1 hour ago",
            type = NotificationType.PAYMENT,
            isRead = true
        ),
        Notification(
            id = "3",
            title = "Lab Report Ready",
            body = "Your blood test results are now available for viewing and download.",
            time = "Yesterday",
            type = NotificationType.APPOINTMENT,
            isRead = true
        )
    )

    val payments = listOf(
        Payment(
            id = "PAY-001",
            description = "Consultation Fee - Dr. Ramesh Iyer",
            amount = 500,
            date = "28 May 2024",
            invoiceId = "INV-2024-8821",
            status = PaymentStatus.PAID
        ),
        Payment(
            id = "PAY-002",
            description = "Lab Test - Blood Profile",
            amount = 1200,
            date = "20 May 2024",
            invoiceId = "INV-2024-8750",
            status = PaymentStatus.PAID
        ),
        Payment(
            id = "PAY-003",
            description = "Pharmacy - Medicines",
            amount = 850,
            date = "15 May 2024",
            invoiceId = "INV-2024-8622",
            status = PaymentStatus.PENDING
        )
    )

    val peakHoursData = listOf(
        "08 AM" to 20,
        "09 AM" to 45,
        "10 AM" to 75,
        "11 AM" to 90,
        "12 PM" to 65,
        "01 PM" to 30,
        "02 PM" to 25,
        "03 PM" to 40,
        "04 PM" to 55,
        "05 PM" to 70,
        "06 PM" to 50,
        "07 PM" to 30
    )
}
