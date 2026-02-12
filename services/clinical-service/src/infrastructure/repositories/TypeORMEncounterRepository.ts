import { Repository } from 'typeorm';
import { Encounter, EncounterStatus } from '@domain/aggregates/Encounter';
import { IEncounterRepository } from '@domain/aggregates/IEncounterRepository';
import { ClinicalNote, NoteType } from '@domain/entities/ClinicalNote';
import { Diagnosis } from '@domain/value-objects/Diagnosis';
import { EncounterEntity } from '@infrastructure/persistence/EncounterEntity';
import { ClinicalNoteEntity } from '@infrastructure/persistence/ClinicalNoteEntity';
import { AppDataSource } from '@infrastructure/persistence/database';

export class TypeORMEncounterRepository implements IEncounterRepository {
    private readonly repo: Repository<EncounterEntity>;

    constructor() {
        this.repo = AppDataSource.getRepository(EncounterEntity);
    }

    async save(encounter: Encounter): Promise<Encounter> {
        const entity = this.toEntity(encounter);
        const saved = await this.repo.save(entity);
        return this.toDomain(saved);
    }

    async findById(id: string): Promise<Encounter | null> {
        const entity = await this.repo.findOne({
            where: { id },
            relations: ['clinicalNotes'],
        });
        return entity ? this.toDomain(entity) : null;
    }

    async findByPatientId(patientId: string): Promise<Encounter[]> {
        const entities = await this.repo.find({
            where: { patientId },
            relations: ['clinicalNotes'],
        });
        return entities.map((e) => this.toDomain(e));
    }

    async findAll(): Promise<Encounter[]> {
        const entities = await this.repo.find({ relations: ['clinicalNotes'] });
        return entities.map((e) => this.toDomain(e));
    }

    private toEntity(encounter: Encounter): EncounterEntity {
        const entity = new EncounterEntity();
        entity.id = encounter.id;
        entity.patientId = encounter.patientId;
        entity.status = encounter.status;
        entity.startDate = encounter.startDate;
        entity.endDate = encounter.endDate;
        entity.diagnoses = encounter.diagnoses.map((d) => ({
            code: d.getCode(),
            description: d.getDescription(),
        }));
        entity.clinicalNotes = encounter.clinicalNotes.map((n) => {
            const noteEntity = new ClinicalNoteEntity();
            noteEntity.id = n.id;
            noteEntity.encounterId = encounter.id;
            noteEntity.content = n.content;
            noteEntity.authorId = n.authorId;
            noteEntity.noteType = n.noteType;
            return noteEntity;
        });
        return entity;
    }

    private toDomain(entity: EncounterEntity): Encounter {
        const clinicalNotes = (entity.clinicalNotes || []).map((n) =>
            ClinicalNote.create({
                id: n.id,
                encounterId: n.encounterId,
                content: n.content,
                authorId: n.authorId,
                noteType: n.noteType as NoteType,
                createdAt: n.createdAt,
            })
        );

        const diagnoses = (entity.diagnoses || []).map((d) =>
            Diagnosis.create(d.code, d.description)
        );

        return Encounter.create({
            id: entity.id,
            patientId: entity.patientId,
            clinicalNotes,
            diagnoses,
            status: entity.status as EncounterStatus,
            startDate: entity.startDate,
            endDate: entity.endDate,
            createdAt: entity.createdAt,
            updatedAt: entity.updatedAt,
        });
    }
}
