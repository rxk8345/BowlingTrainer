import cv2
import numpy as np
import math

"""
    down sample the image so that gutter images are clearer
"""

ANGLE_THRESH_1 = 70

def scale_detect(img):

    height = img.shape[0]
    factor = height / 100
    width = img.shape[1]

    height = height / factor
    width = width / factor

    scaled = cv2.resize(img, (width, height))
    original_scaled = np.copy(scaled)
    blurred = cv2.bilateralFilter(scaled, 3, 100, 1)
    blurred = cv2.blur(blurred, (3,3), 0)
    cv2.imshow("blurred", blurred)

    edges = cv2.Canny(blurred, 40, 120)
    cv2.imshow("canny", edges)

    lines = cv2.HoughLinesP(edges, 1, np.pi/180, 40, minLineLength=40, maxLineGap=3);

    left, right = pick_lines(scaled, lines)

    return original_scaled, left, right


def pick_lines(img, lines):

    height = img.shape[0]
    width = img.shape[1]

    mid_pt = ((width / 2), (height / 2))

    # draw lines on scaled img
    stored_lines = []
    if lines is not None:
        for x1, y1, x2, y2 in lines[0]:
            angle = np.arctan2(y2-y1, x2 - x1)
            angle = angle * 180 / np.pi
            angle = np.round(angle, 2)
            if angle < 0:
                angle += 180

            # keep any lines going down the lane
            if abs(angle - 90) < ANGLE_THRESH_1 :
                if( y1 > y2 ):
                    bp = (x1, y1)
                    tp = (x2, y2)
                else:
                    bp = (x2, y2)
                    tp = (x1, y1)


                dx = abs((x1-x2)/2)
                dy = abs((y1-y2)/2)
                line_mid_pt = (min(x1,x2) + dx, min(y1,y2) + dy)

                dx = math.pow(mid_pt[0] - line_mid_pt[0], 2)
                dy = math.pow(mid_pt[1] - line_mid_pt[1], 2)
                dist = math.sqrt(dx + dy)
                dist = round(dist, 0);

                if line_mid_pt[0] < mid_pt[0]:
                    sign = -1
                else:
                    sign = 1

                stored_lines.append((bp[0], bp[1], tp[0], tp[1], angle, dist, sign))


    x_sorted = sorted(stored_lines, key=lambda tup: tup[5])

    right = None
    left = None

    for i in range(0, len(x_sorted), 1):
        curr = x_sorted[i]
        dir = curr[6]
        if -1 == dir and left is None:
            left = curr
        elif 1 == dir and right is None:
            right = curr
        elif left is not None and right is not None:
            break;

    if left is not None:
        x1 = left[0]
        y1 = left[1]
        x2 = left[2]
        y2 = left[3]
        cv2.line(img, (x1, y1), (x2, y2), (0,255,0), 2)
        cv2.putText(img, "LEFT", (x1,y1), cv2.FONT_HERSHEY_PLAIN, 0.75, (255,0,0), 1)

    if right is not None:
        x1 = right[0]
        y1 = right[1]
        x2 = right[2]
        y2 = right[3]
        cv2.line(img, (x1, y1), (x2, y2), (0,255,0), 2)
        cv2.putText(img, "RIGHT", (x1,y1), cv2.FONT_HERSHEY_PLAIN, 0.75, (255,0,0), 1)

    if left is not None and right is not None:
        # farthest left pt
        x1 = min(left[0], left[2])
        # farthest right pt
        x2 = max(right[0], right[2])

        # a rough crop (doesn't slope)
        rough_crop = img[:, x1:x2]
        cv2.imshow("crop", rough_crop)

    return left, right
