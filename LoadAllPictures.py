import cv2
import numpy as np
from matplotlib import pyplot as plt
import LaneDetector_2 as ld
import SelectPins as pins
import ReversePerspective as rp
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

            disp, left, right = ScaleLaneDetect.scale_detect(img)
            cv2.imshow("disp", disp)

            rp.reverse_rerspective(disp, left, right)



            # quit on 'q' and continue on anything else
            if cv2.waitKey(0) == 113:
                cv2.destroyAllWindows()
                quit(0)
