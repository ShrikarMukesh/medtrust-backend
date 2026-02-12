import { DomainEvent } from './EncounterCreatedEvent';

export class PatientDischargedEvent implements DomainEvent {
    readonly eventType = 'patient.discharged';
    readonly occurredAt: Date;
    readonly payload: Record<string, unknown>;

    constructor(encounterId: string, patientId: string, dischargeDate: Date) {
        this.occurredAt = new Date();
        this.payload = {
            encounterId,
            patientId,
            dischargeDate: dischargeDate.toISOString(),
        };
    }
}
