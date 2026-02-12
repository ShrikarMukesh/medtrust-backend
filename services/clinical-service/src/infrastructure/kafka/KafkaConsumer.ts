import { Kafka, Consumer, EachMessagePayload } from 'kafkajs';

export type MessageHandler = (payload: EachMessagePayload) => Promise<void>;

export class KafkaConsumer {
    private consumer: Consumer;
    private connected = false;

    constructor() {
        const kafka = new Kafka({
            clientId: process.env.KAFKA_CLIENT_ID || 'clinical-service',
            brokers: (process.env.KAFKA_BROKER || 'localhost:9092').split(','),
        });

        this.consumer = kafka.consumer({
            groupId: process.env.KAFKA_GROUP_ID || 'clinical-service-group',
        });
    }

    async connect(): Promise<void> {
        if (!this.connected) {
            await this.consumer.connect();
            this.connected = true;
            console.log('[KafkaConsumer] Connected');
        }
    }

    async subscribe(topic: string, handler: MessageHandler): Promise<void> {
        if (!this.connected) {
            await this.connect();
        }

        await this.consumer.subscribe({ topic, fromBeginning: false });

        await this.consumer.run({
            eachMessage: async (payload) => {
                try {
                    await handler(payload);
                } catch (error) {
                    console.error(`[KafkaConsumer] Error processing message from "${topic}":`, error);
                }
            },
        });

        console.log(`[KafkaConsumer] Subscribed to "${topic}"`);
    }

    async disconnect(): Promise<void> {
        if (this.connected) {
            await this.consumer.disconnect();
            this.connected = false;
            console.log('[KafkaConsumer] Disconnected');
        }
    }
}
