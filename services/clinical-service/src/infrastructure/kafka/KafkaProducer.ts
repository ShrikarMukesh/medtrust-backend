import { Kafka, Producer, ProducerRecord } from 'kafkajs';

export class KafkaProducer {
    private producer: Producer;
    private connected = false;

    constructor() {
        const kafka = new Kafka({
            clientId: process.env.KAFKA_CLIENT_ID || 'clinical-service',
            brokers: (process.env.KAFKA_BROKER || 'localhost:9092').split(','),
        });

        this.producer = kafka.producer();
    }

    async connect(): Promise<void> {
        if (!this.connected) {
            await this.producer.connect();
            this.connected = true;
            console.log('[KafkaProducer] Connected');
        }
    }

    async publish(topic: string, messages: Array<{ key?: string; value: string }>): Promise<void> {
        if (!this.connected) {
            await this.connect();
        }

        const record: ProducerRecord = {
            topic,
            messages,
        };

        await this.producer.send(record);
        console.log(`[KafkaProducer] Published ${messages.length} message(s) to "${topic}"`);
    }

    async disconnect(): Promise<void> {
        if (this.connected) {
            await this.producer.disconnect();
            this.connected = false;
            console.log('[KafkaProducer] Disconnected');
        }
    }
}
