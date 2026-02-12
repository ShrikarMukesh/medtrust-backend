export interface RegisterPatientDTO {
    mrn: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string; // ISO date string
    gender: string;
    contactInfo: {
        phone: string;
        email: string;
        address: string;
    };
}
