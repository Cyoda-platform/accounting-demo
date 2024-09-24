# Accounting Demo Application.

Welcome to the “Accounting-Demo” application for Cyoda, a cloud-based platform designed to simplify building applications using entity-based workflows. This repository is a **Java** example application to connect with a Cyoda environment and demonstrates basic functions through interactive Jupyter notebooks.

Other languages you can find on our [Cyoda-platform GitHub](https://github.com/Cyoda-platform).

## Overview

**This application demonstrates the operation of an accounting application for processing travel expense reports.**

# Prerequisites

1. A Cyoda Platform account with access to an environment (namespace).
2. Python 3.12.3 and Jupyter Notebook installed.
3. Java 17 installed (for the application backend).
4. Access to your Cyoda environment’s API URL and credentials.


## Running in GitHub Codespaces

### Step 1: Clone the Repository

```bash
git clone https://github.com/Cyoda-platform/accounting-demo.git
cd accounting-demo
```

### Step 2: Set Up Environment Variables

In your **GitHub Codespaces**, set the `DEMO_USER_PASSWD` secret to your Cyoda password using the Codespace Secrets feature.

### Step 3: Run the Application in Codespaces

You can run the example Cyoda client directly from a Codespace terminal using Gradle:

```bash
yes | sdk install java 17.0.12-tem && ./gradlew generateProto && ./gradlew bootRun --args="--cyoda.host=https://'${YOUR NAMESPACE}'.cyoda.net --cyoda.name=demo.user --cyoda.password='${DEMO_USER_PASSWD}' --grpc.server.host=grpc-'${YOUR NAMESPACE}'.cyoda.net --grpc.server.port=443 --grpc.server.tls=true"
```

### Step 4: Running the Jupyter Notebooks

#### **Accounting Demo**

We will follow these steps:

1. Import the workflow configuration.
2. Generate sample entities ('employee', 'expense report', and 'payment') to register the entity models.
3. Generate and save multiple instances of the entities ('employee' and 'expense report').
4. Randomly execute possible transitions for 'expense report' entities to simulate the business process.
5. Observe:
    * Interaction with the compute node running in the background.
    * State changes of 'expense report' entities, managed by the workflow.
    * Creation of new 'payment' entities by the compute node when the specified workflow conditions are met.

Start this notebook in your Codespace terminal with:

```bash
jupyter notebook accounting-demo.ipynb
```

### Run it in your favorite IDE

While the notebook provides a good intro to Cyoda, we encourage you to explore the Java application in your own development environment. Running it in your favorite IDE (such as IntelliJ IDEA or Eclipse) will give you a deeper understanding of how the integration works, and allow you to experiment with the core functions of Cyoda in a more flexible way.