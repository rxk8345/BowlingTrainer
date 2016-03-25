import cv2
import numpy as np
"""
    Richy Kapadia
    Idea:
        Run canny and Houghlines 2x
        First for gutter edges
        2nd for foul line edge
"""
ANGLE_THRESH_1 = 60


class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y


class Line:

    def __init__(self, x1, y1, x2, y2):
        if y1 < y2 :
            botPt = Point(x2, y2)
            topPt = Point(x1, y1)
        else:
            botPt = Point(x1, y1)
            topPt = Point(x2, y2)

        self.p1 = botPt
        self.p2 = topPt


def find_lane_edge(img):

    # less than half of the height of the image
    minLineLength = img.shape[0] // 2.5
    #
    maxLineGap = minLineLength // 3.5

    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)

    equalized = cv2.equalizeHist(gray)

    ret, thresh = cv2.threshold(equalized,0,255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    sub = np.subtract(thresh, equalized)

    blur = cv2.GaussianBlur(sub,(9,9),0)
    canny = cv2.Canny(blur, 80, 175, apertureSize=3)
    # Low thresh - start with the possible lines
    # minLineLength - Long enough to be a lane edge
    # maxLineGap - low 10 pixels because we expect the line to be solid
    lines = cv2.HoughLinesP(canny, 1, np.pi/180, threshold=30, minLineLength=minLineLength, maxLineGap=maxLineGap )

    good_lines = []

    for x1, y1, x2, y2 in lines[0]:
        angle = np.arctan2(y2-y1, x2 - x1)
        angle = angle * 180 / np.pi
        angle = np.round(angle, 2)
        if angle < 0:
            angle += 180

        # keep any lines going down the lane
        if abs(angle - 90) < ANGLE_THRESH_1 :
            l = Line(x1, y1, x2, y2)
            good_lines.append(l)

            cv2.line(img, (l.p1.x, l.p1.y), (l.p2.x, l.p2.y), (0,255,0), 2)
            cv2.putText(img, str(angle), (l.p1.x + 5, l.p1.y + 5), cv2.FONT_HERSHEY_PLAIN, 0.75, (255,0,0), 1)
            print str(l.p1.x) + " " + str(l.p1.y) + " " + str(l.p2.x) + " " + str(l.p2.y)

    # sort lines by x value
    good_lines = sorted(good_lines, key=lambda x: x.p1.x)
    pos = 0
    for l in good_lines:
        cv2.putText(img, str(pos), (l.p1.x + 10, l.p1.y - 10), cv2.FONT_HERSHEY_PLAIN, 0.75, (0,0,255), 1)
        pos += 1

    thresh = cv2.erode(thresh, (9,9))
    thresh = cv2.dilate(thresh, (9,9))
    sub = cv2.subtract(thresh, gray)
    cv2.imshow("canny", canny)
    cv2.imshow("sub", sub)

    return good_lines, img


def select_lane(img, good_lines):

    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    ret, thresh = cv2.threshold(gray,0,255,cv2.THRESH_OTSU)

    if len(good_lines) > 2:
        left_edge = good_lines[0]
        for i in range(1, len(good_lines), 1):
            right_edge = good_lines[i]

            # farthest left pt
            x1 = np.min([left_edge.p1.x, left_edge.p2.x])
            # farthest right pt
            x2 = np.max([right_edge.p1.x, right_edge.p2.x])

            # most top pt
            y1 = np.min([left_edge.p1.y, left_edge.p2.y])
            # most bottom pt
            y2 = np.max([right_edge.p1.y, right_edge.p2.y])

            # a rough crop (doesn't slope)
            rough_crop = thresh[y1:y2, x1:x2]

            cv2.imshow("rough crop", rough_crop)
            if cv2.waitKey(0) == 113:
                return

            left_edge = good_lines[i]


if __name__ == '__main__':
    filename = 'pictures/hpl.jpg'
    # filename = 'pictures/pro_anvilane.jpg'
    img = cv2.imread(filename)
    select_copy = np.copy(img)
    edge_copy = np.copy(img)

    lane_edge, l_disp = find_lane_edge(edge_copy)
    cv2.imshow("img", l_disp)

    select_lane(select_copy, lane_edge)

    cv2.waitKey(0)