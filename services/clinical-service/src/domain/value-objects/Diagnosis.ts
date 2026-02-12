export class Diagnosis {
    private readonly code: string;
    private readonly description: string;

    private constructor(code: string, description: string) {
        this.code = code;
        this.description = description;
    }

    static create(code: string, description: string): Diagnosis {
        if (!code || code.trim().length === 0) {
            throw new Error('Diagnosis code cannot be empty');
        }

        // ICD-10 format: letter followed by 2+ digits, optional dot and more digits
        const icd10Pattern = /^[A-Z]\d{2}(\.\d{1,4})?$/;
        if (!icd10Pattern.test(code)) {
            throw new Error(
                'Diagnosis code must be a valid ICD-10 format, e.g. J06.9'
            );
        }

        if (!description || description.trim().length === 0) {
            throw new Error('Diagnosis description cannot be empty');
        }

        return new Diagnosis(code, description);
    }

    getCode(): string {
        return this.code;
    }

    getDescription(): string {
        return this.description;
    }

    equals(other: Diagnosis): boolean {
        return this.code === other.code;
    }

    toString(): string {
        return `${this.code} - ${this.description}`;
    }
}
