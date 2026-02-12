import { v4 as uuidv4 } from 'uuid';

export class PatientId {
    private readonly value: string;

    private constructor(value: string) {
        this.value = value;
    }

    static create(): PatientId {
        return new PatientId(uuidv4());
    }

    static from(value: string): PatientId {
        if (!value || value.trim().length === 0) {
            throw new Error('PatientId cannot be empty');
        }
        return new PatientId(value);
    }

    getValue(): string {
        return this.value;
    }

    equals(other: PatientId): boolean {
        return this.value === other.value;
    }

    toString(): string {
        return this.value;
    }
}
