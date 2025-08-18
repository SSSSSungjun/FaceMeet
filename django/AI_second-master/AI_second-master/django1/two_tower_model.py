import torch
import torch.nn as nn

class TwoTowerModel(nn.Module):
    def __init__(self, input_dim=17, embedding_dim=64):
        super(TwoTowerModel, self).__init__()
        self.A_encoder = nn.Sequential(
            nn.Linear(input_dim, 64),
            nn.ReLU(),
            nn.Linear(64, embedding_dim)
        )
        self.B_encoder = nn.Sequential(
            nn.Linear(input_dim, 64),
            nn.ReLU(),
            nn.Linear(64, embedding_dim)
        )

    def forward(self, A, B):
        A_vec = self.A_encoder(A)
        B_vec = self.B_encoder(B)
        similarity = torch.sum(A_vec * B_vec, dim=1)
        return similarity, A_vec, B_vec
    def encode_A(self, a):
        return self.A_tower(a)

    def encode_B(self, b):
        return self.B_tower(b)