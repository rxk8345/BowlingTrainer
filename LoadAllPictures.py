import cv2
import numpy as np
from matplotlib import pyplot as plt
import LaneDetector_2 as ld
import SelectPins as pins
import os
import ScaleLaneDetect


"""
    Richy Kapadia
    Idea:
        Test ideas and concepts on each still img in the pictures dir
"""



if __name__ == '__main__':
    img_list = []
    pic_dir = os.getcwd() + "/pictures"
    for subdir, dirs, files in os.walk(pic_dir):
        for f in files:
            if f == ".DS_Store":
                continue
            img_list.append(os.path.join(subdir, f))

    print "loaded up " + str(len(img_list)) + " imgs"

    for name in img_list:
        img = cv2.imread(name);
        if img is not None:

            disp = ScaleLaneDetect.scale_detect(img)

            cv2.imshow("disp", disp)

            # quit on 'q' and continue on anything else
            if cv2.waitKey(0) == 113:
                cv2.destroyAllWindows()
                quit(0)




# Test your img concept here
#             # good_lines, disp = ld.find_lane_edge(img)
#
#             # CLAHE OTSU
#             # lab = cv2.cvtColor(img, cv2.COLOR_BGR2LAB)
#             # l = lab[:,:,0]
#             #
#             # clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
#             # cl1 = clahe.apply(l)
#             #
#             # lab[:,:,0] = cl1
#             # bgr = cv2.cvtColor(lab, cv2.COLOR_LAB2BGR)
#             # hsv = cv2.cvtColor(lab, cv2.COLOR_BGR2HSV)
#             #
#             # h = hsv[:,:,0]
#             # s = hsv[:,:,1]
#             # v = hsv[:,:,2]
#             #
#             # ret, thresh = cv2.threshold(h, 0, 255, cv2.THRESH_OTSU)
#
#
#
#
#
#
#             # scale down lane?
#             # dsize = (img.shape[1]//3, img.shape[0]//3)
#             # scaled = cv2.resize(img, dsize)
#             # good_lines, disp = ld.find_lane_edge(scaled)
#
#             # disp = pins.select_pins(img)
#
#             # display img
#
#             median = cv2.medianBlur(img, 9)
#             canny = cv2.Canny(median, 80, 120)
#             minLen = img.shape[0] / 2.5
#             maxGap = img.shape[0] / 30
#             lines = cv2.HoughLinesP(canny, 1, np.pi/180, 40, minLineLength= minLen, maxLineGap=maxGap )
#
#             ## corner time
#
#             r = img.shape[0]
#             mr = r / 2
#
#             bottom = img[mr:r, :, :]
#
#             gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
#
#             gray = np.float32(gray)
#             dst = cv2.cornerHarris(gray,2,3,0.04)
#
#             #result is dilated for marking the corners, not important
#             dst = cv2.dilate(dst,None)
#
#             # Threshold for an optimal value, it may vary depending on the image.
#             img[dst>0.01*dst.max()]=[0,0,255]
#
#             if lines is not None:
#                 i = 0
#                 for x1, y1, x2, y2 in lines[0]:
#                     cv2.line(img, (x1,y1), (x2, y2), (0,255,0), thickness=2)
#                     if y1 < y2:
#                         cv2.putText(img, str(i), (x2 + 5, y2 + 5), cv2.FONT_HERSHEY_PLAIN, 0.75, (255,0,0), 1)
#                     else:
#                         cv2.putText(img, str(i), (x1 + 5, y1 + 5), cv2.FONT_HERSHEY_PLAIN, 0.75, (255,0,0), 1)
#
#                     i += 1