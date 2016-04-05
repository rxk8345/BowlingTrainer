import cv2
import numpy as np


def select_pins(img):
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    thresh = cv2.inRange(hsv, (0,0,175), (100,25,255))
    thresh = cv2.erode(thresh, (3,3))
    thresh = cv2.dilate(thresh, (3,3))
    contours, hierarchy = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)

    cv2.imshow("hsv", hsv)
    cv2.imshow("thresh", thresh)

    for c in contours:
        if cv2.contourArea(c) > 50:
            x, y, w, h = cv2.boundingRect(c)
            cv2.rectangle(img, (x, y), (x + w, y + h), (0,255,0))

    return img