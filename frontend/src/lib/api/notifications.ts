import { apiFetch, SERVICE_URLS, isMockMode } from '../api';

export interface NotificationResponse {
  id: string;
  recipientId: string;
  recipientContact: string;
  channel: string;
  templateName: string;
  subject: string;
  body: string;
  status: string;
  providerMessageId?: string;
  failureReason?: string;
  retryCount: number;
  sourceEvent?: string;
  createdAt: string;
  sentAt?: string;
  updatedAt: string;
}

export interface SendNotificationRequest {
  recipientId: string;
  recipientContact: string;
  channel: 'EMAIL' | 'SMS';
  templateName: string;
  subject: string;
  body: string;
}

const BASE = SERVICE_URLS.notification;

const mockNotifications: NotificationResponse[] = [
  {
    id: 'notif-1',
    recipientId: 'pat-1',
    recipientContact: 'john.doe@example.com',
    channel: 'EMAIL',
    templateName: 'appointment_reminder',
    subject: 'Upcoming Appointment Reminder',
    body: 'Reminder: You have an upcoming consultation with Dr. Alice Smith tomorrow at 10:00 AM.',
    status: 'SENT',
    retryCount: 0,
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    sentAt: new Date(Date.now() - 3550000).toISOString(),
    updatedAt: new Date(Date.now() - 3550000).toISOString(),
  },
  {
    id: 'notif-2',
    recipientId: 'pat-2',
    recipientContact: '+15551234567',
    channel: 'SMS',
    templateName: 'appointment_confirmation',
    subject: 'Appointment Confirmed',
    body: 'Your appointment on Sep 25 at 2:00 PM is confirmed. Reply STOP to cancel.',
    status: 'DELIVERED',
    retryCount: 0,
    createdAt: new Date(Date.now() - 7200000).toISOString(),
    sentAt: new Date(Date.now() - 7180000).toISOString(),
    updatedAt: new Date(Date.now() - 7180000).toISOString(),
  },
];

export async function getNotifications(): Promise<NotificationResponse[]> {
  if (isMockMode()) return mockNotifications;
  return apiFetch<NotificationResponse[]>(BASE, '/api/notifications');
}

export async function getNotification(id: string): Promise<NotificationResponse> {
  if (isMockMode()) {
    const n = mockNotifications.find(x => x.id === id);
    if (!n) throw new Error('Notification not found');
    return n;
  }
  return apiFetch<NotificationResponse>(BASE, `/api/notifications/${id}`);
}

export async function sendNotification(data: SendNotificationRequest): Promise<NotificationResponse> {
  if (isMockMode()) {
    const n: NotificationResponse = {
      id: `notif-${Date.now()}`,
      ...data,
      status: 'SENT',
      retryCount: 0,
      createdAt: new Date().toISOString(),
      sentAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    mockNotifications.unshift(n);
    return n;
  }
  return apiFetch<NotificationResponse>(BASE, '/api/notifications', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
