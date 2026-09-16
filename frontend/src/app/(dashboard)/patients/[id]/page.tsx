'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import styles from './patient-detail.module.css';
import { Header } from '@/components/layout/Header';
import { Card } from '@/components/ui/Card';
import { Badge, statusVariant } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { getPatient, PatientResponse } from '@/lib/api/patients';
import { ArrowLeft, Phone, Mail, MapPin, Heart, AlertTriangle, Shield } from 'lucide-react';

export default function PatientDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [patient, setPatient] = useState<PatientResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (params.id) {
      loadPatient(params.id as string);
    }
  }, [params.id]);

  const loadPatient = async (id: string) => {
    try {
      const data = await getPatient(id);
      setPatient(data);
    } catch {
      console.error('Patient not found');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className={styles.loading}>Loading...</div>;
  if (!patient) return <div className={styles.loading}>Patient not found</div>;

  return (
    <>
      <Header title={patient.fullName} subtitle={`MRN: ${patient.mrn}`} />
      <div className={styles.content}>
        <Button variant="ghost" size="sm" onClick={() => router.back()} icon={<ArrowLeft size={16} />}>
          Back to Patients
        </Button>

        {/* Profile Header */}
        <Card className={styles.profileCard}>
          <div className={styles.profileHeader}>
            <div className={styles.avatar}>
              {patient.firstName[0]}{patient.lastName[0]}
            </div>
            <div className={styles.profileInfo}>
              <h2 className={styles.profileName}>{patient.fullName}</h2>
              <div className={styles.profileMeta}>
                <span>MRN: {patient.mrn}</span>
                <span>DOB: {patient.dateOfBirth}</span>
                <span>{patient.gender}</span>
                <span>Blood: {patient.bloodType?.replace('_', ' ')}</span>
              </div>
              <Badge variant={statusVariant(patient.active ? 'ACTIVE' : 'INACTIVE')} dot>
                {patient.active ? 'Active' : 'Inactive'}
              </Badge>
            </div>
          </div>
        </Card>

        {/* Info Grid */}
        <div className={styles.grid}>
          {/* Contact Info */}
          <Card>
            <h3 className={styles.sectionTitle}><Phone size={16} /> Contact Information</h3>
            <div className={styles.infoGrid}>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Phone</span>
                <span className={styles.infoValue}>{patient.contactInfo.phone}</span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Email</span>
                <span className={styles.infoValue}>{patient.contactInfo.email}</span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Address</span>
                <span className={styles.infoValue}>
                  {patient.contactInfo.address}, {patient.contactInfo.city}, {patient.contactInfo.state} {patient.contactInfo.zipCode}
                </span>
              </div>
            </div>
          </Card>

          {/* Emergency Contact */}
          <Card>
            <h3 className={styles.sectionTitle}><AlertTriangle size={16} /> Emergency Contact</h3>
            <div className={styles.infoGrid}>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Name</span>
                <span className={styles.infoValue}>{patient.emergencyContact.name}</span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Relationship</span>
                <span className={styles.infoValue}>{patient.emergencyContact.relationship}</span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Phone</span>
                <span className={styles.infoValue}>{patient.emergencyContact.phone}</span>
              </div>
            </div>
          </Card>

          {/* Insurance */}
          <Card>
            <h3 className={styles.sectionTitle}><Shield size={16} /> Insurance</h3>
            <div className={styles.infoGrid}>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Provider</span>
                <span className={styles.infoValue}>{patient.insuranceInfo.provider}</span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Policy #</span>
                <span className={styles.infoValue}>{patient.insuranceInfo.policyNumber}</span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Group #</span>
                <span className={styles.infoValue}>{patient.insuranceInfo.groupNumber}</span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>Expires</span>
                <span className={styles.infoValue}>{patient.insuranceInfo.expirationDate}</span>
              </div>
            </div>
          </Card>

          {/* Allergies */}
          <Card>
            <h3 className={styles.sectionTitle}><Heart size={16} /> Allergies</h3>
            {patient.allergies.length > 0 ? (
              <div className={styles.allergyList}>
                {patient.allergies.map((a, i) => (
                  <Badge key={i} variant="danger" size="md">{a}</Badge>
                ))}
              </div>
            ) : (
              <p className={styles.noAllergies}>No known allergies</p>
            )}
          </Card>
        </div>
      </div>
    </>
  );
}
