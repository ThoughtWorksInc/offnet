#!/usr/bin/env bash
nvidia-docker run \
    --volume /etc/passwd:/etc/passwd:ro \
    --volume "$HOME:$HOME" \
    --volume "$PWD:/mnt/project-root" \
    --workdir /mnt/project-root \
    --tty --interactive \
    --init \
    popatry/anaconda-cuda:python3-miniconda-cuda9.0-cudnn7-runtime-ubuntu16.04-anaconda-project-socat \
    su "$(whoami)"
