import { Patient, Gender } from '@domain/entities/Patient';
import { IPatientRepository } from '@domain/entities/IPatientRepository';
import { RegisterPatientDTO } from '@application/dto/RegisterPatientDTO';

export class RegisterPatientUseCase {
    constructor(private readonly patientRepository: IPatientRepository) { }

    async execute(dto: RegisterPatientDTO): Promise<Patient> {
        const existingPatient = await this.patientRepository.findByMrn(dto.mrn);
        if (existingPatient) {
            throw new Error(`Patient with MRN ${dto.mrn} already exists`);
        }

        const patient = Patient.create({
            mrn: dto.mrn,
            firstName: dto.firstName,
            lastName: dto.lastName,
            dateOfBirth: new Date(dto.dateOfBirth),
            gender: dto.gender as Gender,
            contactInfo: dto.contactInfo,
        });

        return this.patientRepository.save(patient);
    }
}
