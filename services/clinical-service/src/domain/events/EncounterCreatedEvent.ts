export interface DomainEvent {
    eventType: string;
    occurredAt: Date;
    payload: Record<string, unknown>;
}

export class EncounterCreatedEvent implements DomainEvent {
    readonly eventType = 'encounter.created';
    readonly occurredAt: Date;
    readonly payload: Record<string, unknown>;

    constructor(encounterId: string, patientId: string) {
        this.occurredAt = new Date();
        this.payload = { encounterId, patientId };
    }
}
