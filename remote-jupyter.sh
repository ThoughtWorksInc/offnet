ssh -L8887:localhost:8887 twer@10.204.11.203 'cd ~/workspace/TheNet; sshfs src twer@10.204.20.111:/Users/twer/workspace/TheNet/src; jupyter notebook --port 8887'
