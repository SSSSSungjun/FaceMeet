import torch
import pandas as pd
import numpy as np
from torch.utils.data import DataLoader, TensorDataset
from two_tower_model import TwoTowerModel

# Load data
df = pd.read_csv("match_dataset.csv")

X_A = df.iloc[:, 0:17].values.astype(np.float32)
X_B = df.iloc[:, 17:34].values.astype(np.float32)
y = df["label"].values.astype(np.float32)

# TensorDataset
dataset = TensorDataset(torch.tensor(X_A), torch.tensor(X_B), torch.tensor(y))
loader = DataLoader(dataset, batch_size=32, shuffle=True)

# Model
model = TwoTowerModel()
criterion = torch.nn.BCEWithLogitsLoss()
optimizer = torch.optim.Adam(model.parameters(), lr=1e-3)

# Train
for epoch in range(20):
    for A, B, label in loader:
        optimizer.zero_grad()
        logits, _, _ = model(A, B)
        loss = criterion(logits, label)
        loss.backward()
        optimizer.step()
    print(f"Epoch {epoch+1}, Loss: {loss.item():.4f}")

# Save model & all B-vectors
torch.save(model.state_dict(), "two_tower_model.pt")
np.save("X_B_vectors.npy", X_B)  # later used for matching
