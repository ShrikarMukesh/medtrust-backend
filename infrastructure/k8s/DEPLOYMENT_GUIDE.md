# MedTrust — Kubernetes Deployment Guide

> A beginner-friendly guide to understanding Helm charts and deploying MedTrust on a Kind (Kubernetes in Docker) cluster.

---

## Table of Contents

1. [What is Helm?](#1-what-is-helm)
2. [Helm Chart Structure Explained](#2-helm-chart-structure-explained)
3. [How MedTrust Uses Helm](#3-how-medtrust-uses-helm)
4. [Prerequisites](#4-prerequisites)
5. [Full Deployment — Step by Step](#5-full-deployment--step-by-step)
6. [Useful Commands Reference](#6-useful-commands-reference)
7. [Troubleshooting](#7-troubleshooting)
8. [Graceful Shutdown & Restart](#8-graceful-shutdown--restart)
9. [API Endpoint Testing](#9-api-endpoint-testing)

---

## 1. What is Helm?

Helm is the **package manager for Kubernetes** — think of it as `apt` for Ubuntu or `npm` for Node.js, but for Kubernetes applications.

### Without Helm (painful)
```bash
# You have to manually apply every YAML file in the right order
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
# Want to change a value? Edit EVERY file manually.
# Want to rollback? Good luck remembering what changed.
```

### With Helm (easy)
```bash
# One command deploys everything
helm install clinical-service ./helm --namespace medtrust

# Change a value? Just update values.yaml
helm upgrade clinical-service ./helm --namespace medtrust

# Rollback? One command
helm rollback clinical-service 1
```

### Key Helm Concepts

| Concept | What It Is | Analogy |
|---|---|---|
| **Chart** | A package of K8s templates | Like a Node.js `package.json` |
| **Release** | A deployed instance of a chart | Like a running Docker container |
| **Values** | Configuration for a chart | Like `.env` file |
| **Template** | K8s YAML with `{{ }}` placeholders | Like a Jinja2/Handlebars template |
| **Revision** | A version of a release | Like a Git commit |

---

## 2. Helm Chart Structure Explained

Here is the structure of `services/clinical-service/helm/`:

```
helm/
├── Chart.yaml              # 1. WHO am I?
├── values.yaml             # 2. WHAT can be configured?
└── templates/              # 3. HOW to deploy?
    ├── _helpers.tpl         #    Reusable template snippets
    ├── deployment.yaml      #    Pod definition (your Java app)
    ├── service.yaml         #    Network exposure (port mapping)
    ├── configmap.yaml       #    Non-secret environment variables
    └── secret.yaml          #    Sensitive data (passwords)
```

### 2.1 `Chart.yaml` — The Identity Card

```yaml
apiVersion: v2
name: clinical-service          # Chart name
description: MedTrust Clinical Service
version: 0.1.0                  # Chart version (for Helm tracking)
appVersion: "1.0.0"             # Your actual app version
```

### 2.2 `values.yaml` — The Configuration File

This is the **most important file** — it's where you configure everything **without touching templates**:

```yaml
replicaCount: 2                 # How many pods to run

image:
  repository: medtrust/clinical-service   # Docker image name
  tag: latest                             # Docker image tag

service:
  type: NodePort                # How to expose (NodePort for local dev)
  port: 8080
  nodePort: 30001               # Accessible at localhost:30001

env:
  DB_HOST: medtrust-postgres    # K8s service name for Postgres
  DB_PORT: "5432"

secrets:
  DB_PASSWORD: changeme         # Auto base64-encoded by template

resources:
  requests:
    cpu: 250m                   # Minimum CPU guaranteed
    memory: 512Mi               # Minimum RAM guaranteed
  limits:
    cpu: 500m                   # Maximum CPU allowed
    memory: 1Gi                 # Maximum RAM allowed
```

**To change any config**, you only edit `values.yaml` — never the templates.

### 2.3 `templates/` — The Kubernetes YAML Templates

Templates use Go templating `{{ }}` to inject values:

```yaml
# templates/deployment.yaml (simplified)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "clinical-service.fullname" . }}  # → "clinical-service"
spec:
  replicas: {{ .Values.replicaCount }}                # → 2
  template:
    spec:
      containers:
        - name: {{ .Chart.Name }}                      # → "clinical-service"
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"  # → "medtrust/clinical-service:latest"
```

### 2.4 How Values Flow into K8s

```
values.yaml          →    templates/          →    Kubernetes
─────────────────         ─────────────────        ─────────────────
replicaCount: 2           {{ .Values.             2 pods created
                            replicaCount }}

DB_PASSWORD: changeme     {{ .Values.secrets.     Secret with base64
                            DB_PASSWORD            encoded password
                            | b64enc }}

nodePort: 30001           {{ .Values.service.     Service listening
                            nodePort }}            on port 30001
```

---

## 3. How MedTrust Uses Helm

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Kind Cluster                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Namespace: medtrust                     │   │
│  │                                                      │   │
│  │  ┌──────────────────┐    ┌──────────────────────┐   │   │
│  │  │  Helm Release:   │    │   Helm Release:      │   │   │
│  │  │  clinical-service│    │  appointment-service  │   │   │
│  │  │  (2 replicas)    │    │  (2 replicas)         │   │   │
│  │  │  NodePort: 30001 │    │  NodePort: 30002      │   │   │
│  │  └────────┬─────────┘    └───────┬──────────────┘   │   │
│  │           │                      │                   │   │
│  │  ┌────────▼──────────────────────▼──────────────┐   │   │
│  │  │         Infrastructure (raw K8s YAML)         │   │   │
│  │  │  ┌──────────┐  ┌──────────┐  ┌────────────┐  │   │   │
│  │  │  │ Postgres │  │ RabbitMQ │  │   Kafka    │  │   │   │
│  │  │  │ :5432    │  │ :5672    │  │   :9092    │  │   │   │
│  │  │  └──────────┘  └──────────┘  └────────────┘  │   │   │
│  │  └──────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  Nodes: 1 control-plane + 2 workers (workload: services)    │
└─────────────────────────────────────────────────────────────┘
         │                               │
    localhost:30001                  localhost:30002
    (clinical-service)              (appointment-service)
```

### What's Deployed with Helm vs Raw YAML

| Component | Deployed With | Why |
|---|---|---|
| `clinical-service` | **Helm** | App code, needs versioning & rollback |
| `appointment-service` | **Helm** | App code, needs versioning & rollback |
| PostgreSQL | Raw K8s YAML | Infra, rarely changes |
| RabbitMQ | Raw K8s YAML | Infra, rarely changes |
| Kafka | Raw K8s YAML | Infra, rarely changes |

---

## 4. Prerequisites

```bash
# Check all tools are installed
docker --version       # Docker Desktop or Docker Engine
kind version           # Kubernetes in Docker
kubectl version        # Kubernetes CLI
helm version           # Helm 3.x
java -version          # Java 21 (for building)
mvn --version          # Maven (for building)
```

---

## 5. Full Deployment — Step by Step

### Step 1: Create the Kind Cluster

```bash
cd ~/2026-Dev/medtrust-backend

# Create cluster with 3 nodes (1 control-plane + 2 workers)
kind create cluster --config infrastructure/k8s/kind-config.yaml --image kindest/node:v1.27.1

# Verify nodes are ready
kubectl get nodes
# Expected:
# NAME                             STATUS   ROLES           AGE   VERSION
# medtrust-cluster-control-plane   Ready    control-plane   1m    v1.27.1
# medtrust-cluster-worker          Ready    <none>          1m    v1.27.1
# medtrust-cluster-worker2         Ready    <none>          1m    v1.27.1
```

### Step 2: Build Docker Images

```bash
# Build clinical-service
docker build -t medtrust/clinical-service:latest services/clinical-service/

# Build appointment-service
docker build -t medtrust/appointment-service:latest services/appointment-service/

# Verify images exist
docker images | grep medtrust
```

### Step 3: Load Images into Kind

> **Why?** Kind runs K8s inside Docker containers. It can't pull from your local Docker daemon by default. You must explicitly load images into the cluster.

```bash
kind load docker-image medtrust/clinical-service:latest --name medtrust-cluster
kind load docker-image medtrust/appointment-service:latest --name medtrust-cluster
```

### Step 4: Deploy Infrastructure (Namespace + Postgres + RabbitMQ + Kafka)

```bash
# Create namespace
kubectl apply -f infrastructure/k8s/namespace.yaml

# Deploy Postgres (with init.sql for DB creation)
kubectl apply -f infrastructure/k8s/postgres.yaml

# Deploy RabbitMQ
kubectl apply -f infrastructure/k8s/rabbitmq.yaml

# Deploy Kafka (KRaft mode, no Zookeeper)
kubectl apply -f infrastructure/k8s/kafka.yaml

# Wait for all infrastructure to be ready
kubectl wait --for=condition=ready pod -l app=medtrust-postgres -n medtrust --timeout=120s
kubectl wait --for=condition=ready pod -l app=medtrust-rabbitmq -n medtrust --timeout=120s
kubectl wait --for=condition=ready pod -l app=medtrust-kafka -n medtrust --timeout=120s
```

### Step 5: Deploy Services with Helm

```bash
# Deploy clinical-service (Helm install)
helm install clinical-service services/clinical-service/helm/ --namespace medtrust

# Deploy appointment-service (Helm install)
helm install appointment-service services/appointment-service/helm/ --namespace medtrust

# Verify Helm releases
helm list -n medtrust
# Expected:
# NAME                 NAMESPACE  REVISION  STATUS    CHART
# clinical-service     medtrust   1         deployed  clinical-service-0.1.0
# appointment-service  medtrust   1         deployed  appointment-service-0.1.0
```

### Step 6: Verify Everything

```bash
# Check all pods are Running
kubectl get pods -n medtrust

# Check services and ports
kubectl get svc -n medtrust

# Test health endpoints
curl http://localhost:30001/actuator/health   # clinical-service
curl http://localhost:30002/actuator/health   # appointment-service

# Check logs
kubectl logs -l app=clinical-service -n medtrust --tail=20
kubectl logs -l app=appointment-service -n medtrust --tail=20
```

### One-Command Deploy (Automated)

```bash
# Instead of steps 1-6, just run:
chmod +x infrastructure/k8s/deploy.sh
./infrastructure/k8s/deploy.sh
```

---

## 6. Useful Commands Reference

### Helm Commands

```bash
# ── Install / Upgrade ──
helm install <release> <chart-path> -n <namespace>      # First time install
helm upgrade <release> <chart-path> -n <namespace>      # Update existing
helm upgrade --install <release> <chart-path> -n <ns>   # Install or upgrade (idempotent)

# ── Status / List ──
helm list -n medtrust                                   # List all releases
helm status clinical-service -n medtrust                # Detailed release status
helm get values clinical-service -n medtrust            # See deployed values
helm get manifest clinical-service -n medtrust          # See generated YAML

# ── Rollback ──
helm history clinical-service -n medtrust               # See all revisions
helm rollback clinical-service 1 -n medtrust            # Rollback to revision 1

# ── Debug / Test ──
helm template clinical-service ./helm                   # Render templates locally (no deploy)
helm lint ./helm                                        # Validate chart syntax
helm install --dry-run --debug <release> ./helm         # Simulate install

# ── Uninstall ──
helm uninstall clinical-service -n medtrust             # Remove release
```

### Kubectl Commands

```bash
# ── Pods ──
kubectl get pods -n medtrust                            # List pods
kubectl get pods -n medtrust -o wide                    # With node info
kubectl describe pod <pod-name> -n medtrust             # Detailed pod info
kubectl logs <pod-name> -n medtrust                     # Pod logs
kubectl logs -l app=clinical-service -n medtrust        # Logs by label
kubectl exec -it <pod-name> -n medtrust -- /bin/sh      # Shell into pod

# ── Services ──
kubectl get svc -n medtrust                             # List services
kubectl port-forward svc/clinical-service 8080:8080 -n medtrust  # Port forward

# ── Deployments ──
kubectl get deployments -n medtrust                     # List deployments
kubectl scale deployment clinical-service --replicas=3 -n medtrust  # Scale
kubectl rollout restart deployment/clinical-service -n medtrust     # Restart
kubectl rollout status deployment/clinical-service -n medtrust      # Watch rollout

# ── Secrets & ConfigMaps ──
kubectl get secrets -n medtrust                         # List secrets
kubectl get configmaps -n medtrust                      # List configmaps
kubectl get secret clinical-service-secret -n medtrust -o jsonpath='{.data.DB_PASSWORD}' | base64 -d
                                                        # Decode a secret value

# ── Cluster ──
kubectl get nodes                                       # List nodes
kubectl get all -n medtrust                             # Everything in namespace
kubectl top pods -n medtrust                            # Resource usage
```

### Kind Commands

```bash
kind get clusters                                       # List clusters
kind delete cluster --name medtrust-cluster             # Delete cluster
kind load docker-image <image> --name medtrust-cluster  # Load image
kind get kubeconfig --name medtrust-cluster              # Get kubeconfig
```

---

## 7. Troubleshooting

### Pod stuck in `CrashLoopBackOff`

```bash
# Check WHY it's crashing
kubectl describe pod <pod-name> -n medtrust    # Look at "Events" section
kubectl logs <pod-name> -n medtrust            # Check application logs
kubectl logs <pod-name> -n medtrust --previous # Logs from crashed container
```

### Pod stuck in `Pending`

```bash
# Usually insufficient resources or node selector mismatch
kubectl describe pod <pod-name> -n medtrust    # Look for "FailedScheduling"
kubectl get nodes --show-labels                # Check node labels
```

### Service not accessible on localhost

```bash
# Verify service type is NodePort
kubectl get svc -n medtrust
# Check if nodePort matches kind-config.yaml extraPortMappings
# Try port-forwarding as a workaround:
kubectl port-forward svc/clinical-service 8080:8080 -n medtrust
```

### Image not found / `ErrImagePull`

```bash
# Make sure image was loaded into Kind
docker images | grep medtrust                          # Image exists locally?
kind load docker-image medtrust/clinical-service:latest --name medtrust-cluster
```

### Want to redeploy after code change?

```bash
# 1. Rebuild image
docker build -t medtrust/clinical-service:latest services/clinical-service/

# 2. Reload into Kind
kind load docker-image medtrust/clinical-service:latest --name medtrust-cluster

# 3. Restart pods to pick up new image
kubectl rollout restart deployment/clinical-service -n medtrust
```

---

## 8. Graceful Shutdown & Restart

### Shutdown (before switching off machine)

```bash
# Step 1: Scale down app services first (graceful pod termination)
kubectl scale deployment appointment-service clinical-service \
  --replicas=0 -n medtrust --context kind-medtrust-cluster

# Step 2: Scale down infrastructure
kubectl scale deployment medtrust-kafka medtrust-rabbitmq medtrust-postgres \
  --replicas=0 -n medtrust --context kind-medtrust-cluster

# Step 3: Stop the Kind cluster containers (preserves data)
docker stop medtrust-cluster-control-plane medtrust-cluster-worker medtrust-cluster-worker2
```

> **Note:** `docker stop` preserves the cluster state — your data, ConfigMaps, Secrets, and PVCs all remain intact.

### Restart (after machine boot)

```bash
# Step 1: Start Docker containers
docker start medtrust-cluster-control-plane medtrust-cluster-worker medtrust-cluster-worker2

# Step 2: Wait ~30s for nodes to become Ready
kubectl get nodes --context kind-medtrust-cluster
# Wait until all 3 nodes show STATUS: Ready

# Step 3: Start infrastructure first (order matters)
kubectl scale deployment medtrust-postgres --replicas=1 -n medtrust
kubectl wait --for=condition=ready pod -l app=medtrust-postgres -n medtrust --timeout=120s

kubectl scale deployment medtrust-rabbitmq --replicas=1 -n medtrust
kubectl wait --for=condition=ready pod -l app=medtrust-rabbitmq -n medtrust --timeout=120s

kubectl scale deployment medtrust-kafka --replicas=1 -n medtrust
kubectl wait --for=condition=ready pod -l app=medtrust-kafka -n medtrust --timeout=120s

# Step 4: Start application services
kubectl scale deployment clinical-service appointment-service \
  --replicas=1 -n medtrust

# Step 5: Verify everything
kubectl get pods -n medtrust
curl http://localhost:30001/actuator/health
curl http://localhost:30002/actuator/health
```

### Full Teardown (delete everything)

```bash
# Option A: Use the teardown script
./infrastructure/k8s/teardown.sh

# Option B: Manual
helm uninstall clinical-service -n medtrust
helm uninstall appointment-service -n medtrust
kind delete cluster --name medtrust-cluster
```

---

## 9. API Endpoint Testing

### Clinical Service (port 30001)

#### Health Check
```bash
curl http://localhost:30001/actuator/health | python3 -m json.tool
```

#### Register a Patient
```bash
curl -s -X POST http://localhost:30001/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "mrn": "MRN-001",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE",
    "contactInfo": {
      "phone": "+1234567890",
      "email": "john.doe@example.com",
      "address": "123 Main Street, Bangalore"
    }
  }' | python3 -m json.tool
```

#### Get Patient by ID
```bash
curl http://localhost:30001/api/patients/{patient-id} | python3 -m json.tool
```

#### Create an Encounter
```bash
curl -s -X POST http://localhost:30001/api/encounters \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patient-id}",
    "type": "OUTPATIENT",
    "department": "Cardiology",
    "primaryPhysician": "Dr. Smith"
  }' | python3 -m json.tool
```

#### Get Encounter by ID
```bash
curl http://localhost:30001/api/encounters/{encounter-id} | python3 -m json.tool
```

#### Admit Patient (update encounter status)
```bash
curl -s -X PUT http://localhost:30001/api/encounters/{encounter-id}/admit | python3 -m json.tool
```

#### Discharge Patient
```bash
curl -s -X PUT http://localhost:30001/api/encounters/{encounter-id}/discharge | python3 -m json.tool
```

#### Add Clinical Note to Encounter
```bash
curl -s -X POST http://localhost:30001/api/encounters/{encounter-id}/notes \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Patient presenting with chest pain",
    "authorId": "dr-smith-001"
  }' | python3 -m json.tool
```

### Appointment Service (port 30002)

#### Health Check
```bash
curl http://localhost:30002/actuator/health | python3 -m json.tool
```

#### Create an Appointment
```bash
curl -s -X POST http://localhost:30002/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patient-id}",
    "providerId": "dr-smith-001",
    "startTime": "2026-03-01T10:00:00Z",
    "endTime": "2026-03-01T10:30:00Z",
    "type": "CONSULTATION",
    "reason": "Follow-up checkup"
  }' | python3 -m json.tool
```

#### Get Appointment by ID
```bash
curl http://localhost:30002/api/appointments/{appointment-id} | python3 -m json.tool
```

#### Confirm Appointment
```bash
curl -s -X PUT http://localhost:30002/api/appointments/{appointment-id}/confirm | python3 -m json.tool
```

#### Reschedule Appointment
```bash
curl -s -X PUT http://localhost:30002/api/appointments/{appointment-id}/reschedule \
  -H "Content-Type: application/json" \
  -d '{
    "newStartTime": "2026-03-02T14:00:00Z",
    "newEndTime": "2026-03-02T14:30:00Z",
    "reason": "Patient requested change"
  }' | python3 -m json.tool
```

#### Cancel Appointment
```bash
curl -s -X PUT "http://localhost:30002/api/appointments/{appointment-id}/cancel?reason=Patient+no+show" | python3 -m json.tool
```

#### Complete Appointment
```bash
curl -s -X PUT http://localhost:30002/api/appointments/{appointment-id}/complete | python3 -m json.tool
```

#### Test SMS via RabbitMQ
```bash
curl -s -X POST "http://localhost:30002/api/appointments/test-sms?phone=+1234567890&msg=Hello+from+MedTrust" | python3 -m json.tool
```

---

## Quick Reference Card

| Task | Command |
|---|---|
| **Deploy everything** | `./infrastructure/k8s/deploy.sh` |
| **Tear down everything** | `./infrastructure/k8s/teardown.sh` |
| **Graceful shutdown** | Scale → `docker stop` (see Section 8) |
| **Restart after boot** | `docker start` → Scale (see Section 8) |
| **Check pod status** | `kubectl get pods -n medtrust` |
| **Check health** | `curl localhost:30001/actuator/health` |
| **View logs** | `kubectl logs -l app=clinical-service -n medtrust` |
| **Redeploy after code change** | Rebuild → Load → `kubectl rollout restart` |
| **Rollback** | `helm rollback clinical-service 1 -n medtrust` |
| **Scale up** | `kubectl scale deployment clinical-service --replicas=3 -n medtrust` |
