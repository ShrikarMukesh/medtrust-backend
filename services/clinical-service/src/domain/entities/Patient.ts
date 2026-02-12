import { v4 as uuidv4 } from 'uuid';

export enum Gender {
    MALE = 'MALE',
    FEMALE = 'FEMALE',
    OTHER = 'OTHER',
    UNKNOWN = 'UNKNOWN',
}

export interface ContactInfo {
    phone: string;
    email: string;
    address: string;
}

export interface PatientProps {
    id?: string;
    mrn: string;
    firstName: string;
    lastName: string;
    dateOfBirth: Date;
    gender: Gender;
    contactInfo: ContactInfo;
    createdAt?: Date;
    updatedAt?: Date;
}

export class Patient {
    readonly id: string;
    readonly mrn: string;
    readonly firstName: string;
    readonly lastName: string;
    readonly dateOfBirth: Date;
    readonly gender: Gender;
    readonly contactInfo: ContactInfo;
    readonly createdAt: Date;
    readonly updatedAt: Date;

    private constructor(props: Required<PatientProps>) {
        this.id = props.id;
        this.mrn = props.mrn;
        this.firstName = props.firstName;
        this.lastName = props.lastName;
        this.dateOfBirth = props.dateOfBirth;
        this.gender = props.gender;
        this.contactInfo = props.contactInfo;
        this.createdAt = props.createdAt;
        this.updatedAt = props.updatedAt;
    }

    static create(props: PatientProps): Patient {
        return new Patient({
            id: props.id ?? uuidv4(),
            mrn: props.mrn,
            firstName: props.firstName,
            lastName: props.lastName,
            dateOfBirth: props.dateOfBirth,
            gender: props.gender,
            contactInfo: props.contactInfo,
            createdAt: props.createdAt ?? new Date(),
            updatedAt: props.updatedAt ?? new Date(),
        });
    }

    getFullName(): string {
        return `${this.firstName} ${this.lastName}`;
    }
}
