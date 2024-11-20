#!/bin/bash

# Accept the name of the application as a parameter
APP_NAME=$1
if [ -z "$APP_NAME" ]; then
  echo "Error: Application name parameter is required."
  exit 1
fi

# Define the Kubernetes resources in the YAML format
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: ${APP_NAME}-pod
  namespace: default
spec:
  containers:
  - name: ${APP_NAME}-nginx
    image: nginx:latest
---
apiVersion: v1
kind: Service
metadata:
  name: ${APP_NAME}-service
spec:
  selector:
    app: ${APP_NAME}
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
  type: ClusterIP
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${APP_NAME}-deployment
spec:
  replicas: 3  # You can modify the number of replicas here to scale
  selector:
    matchLabels:
      app: ${APP_NAME}
  template:
    metadata:
      labels:
        app: ${APP_NAME}
    spec:
      containers:
      - name: ${APP_NAME}-nginx
        image: nginx:latest
        ports:
        - containerPort: 80
EOF

echo "Kubernetes resources created: Pod, Service, and Deployment for ${APP_NAME}."

