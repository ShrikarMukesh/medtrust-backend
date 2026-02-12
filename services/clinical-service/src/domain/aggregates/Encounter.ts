import { v4 as uuidv4 } from 'uuid';
import { ClinicalNote, ClinicalNoteProps } from '@domain/entities/ClinicalNote';
import { Diagnosis } from '@domain/value-objects/Diagnosis';
import { DomainEvent } from '@domain/events/EncounterCreatedEvent';
import { EncounterCreatedEvent } from '@domain/events/EncounterCreatedEvent';
import { PatientAdmittedEvent } from '@domain/events/PatientAdmittedEvent';
import { PatientDischargedEvent } from '@domain/events/PatientDischargedEvent';

export enum EncounterStatus {
    PLANNED = 'PLANNED',
    IN_PROGRESS = 'IN_PROGRESS',
    COMPLETED = 'COMPLETED',
    CANCELLED = 'CANCELLED',
}

export interface EncounterProps {
    id?: string;
    patientId: string;
    clinicalNotes?: ClinicalNote[];
    diagnoses?: Diagnosis[];
    status?: EncounterStatus;
    startDate?: Date;
    endDate?: Date | null;
    createdAt?: Date;
    updatedAt?: Date;
}

export class Encounter {
    readonly id: string;
    readonly patientId: string;
    private _clinicalNotes: ClinicalNote[];
    private _diagnoses: Diagnosis[];
    private _status: EncounterStatus;
    readonly startDate: Date;
    private _endDate: Date | null;
    readonly createdAt: Date;
    private _updatedAt: Date;

    private _domainEvents: DomainEvent[] = [];

    private constructor(props: Required<Omit<EncounterProps, 'endDate'>> & { endDate: Date | null }) {
        this.id = props.id;
        this.patientId = props.patientId;
        this._clinicalNotes = props.clinicalNotes;
        this._diagnoses = props.diagnoses;
        this._status = props.status;
        this.startDate = props.startDate;
        this._endDate = props.endDate;
        this.createdAt = props.createdAt;
        this._updatedAt = props.updatedAt;
    }

    static create(props: EncounterProps): Encounter {
        const encounter = new Encounter({
            id: props.id ?? uuidv4(),
            patientId: props.patientId,
            clinicalNotes: props.clinicalNotes ?? [],
            diagnoses: props.diagnoses ?? [],
            status: props.status ?? EncounterStatus.PLANNED,
            startDate: props.startDate ?? new Date(),
            endDate: props.endDate ?? null,
            createdAt: props.createdAt ?? new Date(),
            updatedAt: props.updatedAt ?? new Date(),
        });

        encounter.addDomainEvent(
            new EncounterCreatedEvent(encounter.id, encounter.patientId)
        );

        return encounter;
    }

    // ── Getters ──

    get status(): EncounterStatus {
        return this._status;
    }

    get endDate(): Date | null {
        return this._endDate;
    }

    get clinicalNotes(): ReadonlyArray<ClinicalNote> {
        return [...this._clinicalNotes];
    }

    get diagnoses(): ReadonlyArray<Diagnosis> {
        return [...this._diagnoses];
    }

    get updatedAt(): Date {
        return this._updatedAt;
    }

    // ── Domain behaviour ──

    admit(): void {
        if (this._status !== EncounterStatus.PLANNED) {
            throw new Error('Only planned encounters can be admitted');
        }

        this._status = EncounterStatus.IN_PROGRESS;
        this._updatedAt = new Date();

        this.addDomainEvent(
            new PatientAdmittedEvent(this.id, this.patientId)
        );
    }

    addNote(props: Omit<ClinicalNoteProps, 'encounterId'>): ClinicalNote {
        if (this._status === EncounterStatus.COMPLETED || this._status === EncounterStatus.CANCELLED) {
            throw new Error('Cannot add notes to a completed or cancelled encounter');
        }

        const note = ClinicalNote.create({
            ...props,
            encounterId: this.id,
        });

        this._clinicalNotes.push(note);
        this._updatedAt = new Date();
        return note;
    }

    addDiagnosis(diagnosis: Diagnosis): void {
        if (this._status === EncounterStatus.COMPLETED || this._status === EncounterStatus.CANCELLED) {
            throw new Error('Cannot add diagnoses to a completed or cancelled encounter');
        }

        const exists = this._diagnoses.some(d => d.equals(diagnosis));
        if (exists) {
            throw new Error(`Diagnosis ${diagnosis.getCode()} already exists on this encounter`);
        }

        this._diagnoses.push(diagnosis);
        this._updatedAt = new Date();
    }

    discharge(): void {
        if (this._status !== EncounterStatus.IN_PROGRESS) {
            throw new Error('Only in-progress encounters can be discharged');
        }

        this._status = EncounterStatus.COMPLETED;
        this._endDate = new Date();
        this._updatedAt = new Date();

        this.addDomainEvent(
            new PatientDischargedEvent(this.id, this.patientId, this._endDate)
        );
    }

    cancel(): void {
        if (this._status === EncounterStatus.COMPLETED) {
            throw new Error('Completed encounters cannot be cancelled');
        }

        this._status = EncounterStatus.CANCELLED;
        this._updatedAt = new Date();
    }

    // ── Domain events ──

    private addDomainEvent(event: DomainEvent): void {
        this._domainEvents.push(event);
    }

    pullDomainEvents(): DomainEvent[] {
        const events = [...this._domainEvents];
        this._domainEvents = [];
        return events;
    }
}
