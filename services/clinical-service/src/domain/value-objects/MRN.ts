export class MRN {
    private readonly value: string;

    private constructor(value: string) {
        this.value = value;
    }

    static create(value: string): MRN {
        if (!value || value.trim().length === 0) {
            throw new Error('MRN cannot be empty');
        }

        const mrnPattern = /^MRN-\d{6,10}$/;
        if (!mrnPattern.test(value)) {
            throw new Error(
                'MRN must follow the format MRN-XXXXXX (6–10 digits), e.g. MRN-000001'
            );
        }

        return new MRN(value);
    }

    getValue(): string {
        return this.value;
    }

    equals(other: MRN): boolean {
        return this.value === other.value;
    }

    toString(): string {
        return this.value;
    }
}
