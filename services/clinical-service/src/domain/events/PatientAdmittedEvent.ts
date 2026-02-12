import { DomainEvent } from './EncounterCreatedEvent';

export class PatientAdmittedEvent implements DomainEvent {
    readonly eventType = 'patient.admitted';
    readonly occurredAt: Date;
    readonly payload: Record<string, unknown>;

    constructor(encounterId: string, patientId: string) {
        this.occurredAt = new Date();
        this.payload = { encounterId, patientId };
    }
}
