#!/bin/bash

sudo cp cups-scraper.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now cups-scraper.service