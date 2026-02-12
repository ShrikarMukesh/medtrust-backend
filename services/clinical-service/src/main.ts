import 'reflect-metadata';
import dotenv from 'dotenv';
dotenv.config();

import express from 'express';
import cors from 'cors';
import helmet from 'helmet';

import { AppDataSource } from '@infrastructure/persistence/database';
import { KafkaProducer } from '@infrastructure/kafka/KafkaProducer';
import { KafkaConsumer } from '@infrastructure/kafka/KafkaConsumer';

import { TypeORMPatientRepository } from '@infrastructure/repositories/TypeORMPatientRepository';
import { TypeORMEncounterRepository } from '@infrastructure/repositories/TypeORMEncounterRepository';

import { RegisterPatientUseCase } from '@application/use-cases/RegisterPatientUseCase';
import { CreateEncounterUseCase } from '@application/use-cases/CreateEncounterUseCase';
import { AddClinicalNoteUseCase } from '@application/use-cases/AddClinicalNoteUseCase';
import { GetEncounterUseCase } from '@application/use-cases/GetEncounterUseCase';

import { PatientController } from '@interfaces/controllers/PatientController';
import { EncounterController } from '@interfaces/controllers/EncounterController';
import { createPatientRoutes } from '@interfaces/routes/patientRoutes';
import { createEncounterRoutes } from '@interfaces/routes/encounterRoutes';

const PORT = parseInt(process.env.PORT || '3001', 10);

async function bootstrap(): Promise<void> {
    // ── Database ──
    await AppDataSource.initialize();
    console.log('[DB] DataSource initialized');

    // ── Kafka ──
    const kafkaProducer = new KafkaProducer();
    const kafkaConsumer = new KafkaConsumer();

    try {
        await kafkaProducer.connect();
        await kafkaConsumer.connect();
    } catch (err) {
        console.warn('[Kafka] Could not connect — running without messaging:', err);
    }

    // ── Repositories ──
    const patientRepository = new TypeORMPatientRepository();
    const encounterRepository = new TypeORMEncounterRepository();

    // ── Use cases ──
    const registerPatientUseCase = new RegisterPatientUseCase(patientRepository);
    const createEncounterUseCase = new CreateEncounterUseCase(encounterRepository, patientRepository);
    const addClinicalNoteUseCase = new AddClinicalNoteUseCase(encounterRepository);
    const getEncounterUseCase = new GetEncounterUseCase(encounterRepository);

    // ── Controllers ──
    const patientController = new PatientController(registerPatientUseCase);
    const encounterController = new EncounterController(
        createEncounterUseCase,
        addClinicalNoteUseCase,
        getEncounterUseCase,
    );

    // ── Express app ──
    const app = express();

    app.use(helmet());
    app.use(cors());
    app.use(express.json());

    // Health check
    app.get('/health', (_req, res) => {
        res.json({ status: 'UP', service: 'clinical-service' });
    });

    // Routes
    app.use('/api/patients', createPatientRoutes(patientController));
    app.use('/api/encounters', createEncounterRoutes(encounterController));

    // ── Start ──
    app.listen(PORT, () => {
        console.log(`[clinical-service] Running on port ${PORT}`);
    });

    // Graceful shutdown
    const shutdown = async () => {
        console.log('[clinical-service] Shutting down...');
        await kafkaProducer.disconnect();
        await kafkaConsumer.disconnect();
        await AppDataSource.destroy();
        process.exit(0);
    };

    process.on('SIGINT', shutdown);
    process.on('SIGTERM', shutdown);
}

bootstrap().catch((err) => {
    console.error('[clinical-service] Failed to start:', err);
    process.exit(1);
});
