import torch

class OffConv2d(torch.nn.Module):
    def __init__(self, in_channels, out_channels,
                 kernel_size,
                 stride=1,
                 padding=0,
                 dilation=1,
                 groups=1,
                 bias=True):
        super().__init__()
        self.score = torch.nn.Conv2d(
            in_channels,
            out_channels,
            kernel_size=kernel_size,
            stride=stride,
            padding=1,
            dilation=dilation,
            groups=groups,
            bias=bias
        )
        self.offset_x = torch.nn.Conv2d(
            in_channels,
            out_channels,
            kernel_size=kernel_size,
            stride=stride,
            padding=1,
            dilation=dilation,
            groups=groups,
            bias=bias
        )
        self.offset_y = torch.nn.Conv2d(
            in_channels,
            out_channels,
            kernel_size=kernel_size,
            stride=stride,
            padding=1,
            dilation=dilation,
            groups=groups,
            bias=bias
        )
    def forward(self, x):
        original_score = self.score(x)
        batch_size, number_of_channels, height, width = original_score.size()
        return torch.nn.functional.grid_sample(
            original_score.view(-1, 1, height, width),
            torch.stack(
                (
                    self.offset_x(x).view(-1, height, width),
                    self.offset_y(x).view(-1, height, width)
                ),
                dim=3
            )
        ).view(batch_size, number_of_channels, height, width)
    
import torch.nn as nn
import torch.nn.functional as F


class Net(nn.Module):
    def __init__(self):
        super().__init__()
        self.conv1 = OffConv2d(1, 6, 5)
        self.pool = nn.MaxPool2d(2, 2)
        self.conv2 = OffConv2d(6, 16, 5)
        self.fc1 = nn.Linear(16 * 5 * 5, 120)
        self.fc2 = nn.Linear(120, 84)
        self.fc3 = nn.Linear(84, 10)

    def forward(self, x):
        batch_size, height, width = x.size()
        x = x.view(batch_size, 1, height, width)
        x = self.pool(F.relu(self.conv1(x)))
        x = self.pool(F.relu(self.conv2(x)))
        x = x.view(-1, 16 * 5 * 5)
        x = F.relu(self.fc1(x))
        x = F.relu(self.fc2(x))
        x = self.fc3(x)
        x = F.softmax(x, dim=1)
        return x
