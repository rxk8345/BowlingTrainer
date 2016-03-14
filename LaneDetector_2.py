import cv2
import numpy as np
"""
    Richy Kapadia
    Idea:
        Run canny and Houghlines 2x
        First for gutter edges
        2nd for foul line edge
"""
ANGLE_THRESH_1 = 30

class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y

class Line:
    def __init__(self, p1, p2):
        self.p1 = p1
        self.p2 = p2

    def __init__(self, x1, y1, x2, y2):
        self.p1 = Point(x1, y1)
        self.p2 = Point(x2,y2)


def find_lane_edge(img):
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray,(5,5),0)
    canny = cv2.Canny(blur, 100, 200, apertureSize=3)
    # Low thresh - start with the possible lines
    # minLineLength - Long enough to be a lane edge
    # maxLineGap - low 10 pixels because we expect the line to be solid
    lines = cv2.HoughLinesP(canny, 1, np.pi/180, threshold=40, minLineLength=175, maxLineGap=15 )

    good_lines = []

    for x1, y1, x2, y2 in lines[0]:
        angle = np.arctan2(y2-y1, x2 - x1)
        angle = angle * 180 / np.pi
        angle = np.round(angle, 2)
        if angle < 0:
            angle += 180

        # keep any lines going down the lane
        if abs(angle - 90) < ANGLE_THRESH_1 :
            good_lines.append(Line(x1, y1, x2, y2))

    for l in good_lines:
        x1 = l.p1.x
        y1 = l.p1.y
        x2 = l.p2.x
        y2 = l.p2.y
        cv2.line(img, (x1, y1), (x2, y2), (0,255,0), 2)
        cv2.putText(img, str(angle), (x1 + 5, y1 + 5), cv2.FONT_HERSHEY_PLAIN, 0.75, (255,0,0), 1)
        print str(x1) + " " + str(y1) + " " + str(x2) + " " + str(y2)

    cv2.imshow("canny", canny)

    return good_lines, img


def find_foul_line(img, gutter_edges):
    """
    Find foul line region on interest based on the detected edges going down the lane
    Run canny again with lower thresholds
    Filter on horizontal angles
    :param img:
    :param gutter_edges:
    :return:
    """

    # find all the bottom points
    bottom_pts = []
    for edge in gutter_edges:
        if edge.p1.y > edge.p2.y:
            # p1 is closer to the bottom
            bottom_pts.append(Point(edge.p1.x, edge.p1.y))
        else:
            # p2 is closer to the bottom
            bottom_pts.append(Point(edge.p2.x, edge.p2.y))

    # find the min pt and max pt out of the bottom pts
    tl_x = 0
    tl_y = 0
    br_x = img.shape[0]
    br_y = img.shape[1]
    # for pt in bottom_pts:


if __name__ == '__main__':
    filename = 'pictures/hpl.jpg'
    # filename = 'pictures/pro_anvilane.jpg'
    img = cv2.imread(filename)
    foul_copy = np.copy(img)
    edge_copy = np.copy(img)

    lane_edge, l_disp = find_lane_edge(edge_copy)
    find_foul_line(foul_copy, lane_edge)

    cv2.imshow("img", l_disp)
    cv2.waitKey(0)