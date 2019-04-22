from sense_hat import SenseHat
import os
import time
import pygame
import datetime
import sys
import fileinput
from pygame.locals import *


time.sleep(1)

pygame.init()
pygame.display.set_mode((640, 480))

sense = SenseHat()
f=open('statusOfJoystick.txt','w')
f.write("Nothing\n")
f.write("Nothing\n")
f.close()

def handle_event(event):
    if event.key == pygame.K_DOWN:
        print ("Down")
        ts = time.time()
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S.%f')
        f=open('statusOfJoystick.txt','a')
        f.write('free '+ st + '\n');
        f.close()
        maintainance()
    elif event.key == pygame.K_UP:
        print ("Up")
        ts = time.time()
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S.%f')
        f=open('statusOfJoystick.txt','a')
        f.write('occupied '+ st + '\n');
        f.close()
        maintainance()
    elif event.key == pygame.K_LEFT:
        print ("Not Used")
    elif event.key == pygame.K_RIGHT:
        print ("Not Used")
    elif event.key == pygame.K_RETURN:
        running = False

def maintainance():
    noOfUpdates=0
    f=open('statusOfJoystick.txt','r')
    for line in f:
        noOfUpdates+=1;
    f.close()
    if noOfUpdates>2:
        delete(noOfUpdates)

def delete(noOfUpdates):
    for i in range(1,noOfUpdates-1):
        for line_number, line in enumerate(fileinput.input('statusOfJoystick.txt', inplace=i)):
            if line_number == 0:
                continue
            else:
                sys.stdout.write(line)

running = True
while running:
    for event in pygame.event.get():
        if event.type == QUIT:
            running = False
        if event.type == KEYDOWN:
            if event.key == K_ESCAPE:
                running = False
            handle_event(event)
