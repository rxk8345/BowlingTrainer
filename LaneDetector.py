import cv2
import numpy as np
"""
    Richy Kapadia

    Idea:
        Harris corner - Find all corners.... there will be a lot of them
        Canny and HoughLines - With a high threshold and long line length
        These edges most likely will be the edge of the lane and the foul line

        Filter out the corners by which ones are a long one of theses edges

"""

class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y


def findCorners(img):
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    gray = np.float32(gray)
    corners = cv2.cornerHarris(gray,2,9,0.04)

    # result is dilated for marking the corners
    # this will allow lines have a larger corner to hit
    dst = cv2.dilate(corners,None)

    copy = np.copy(img)

    # Thresholds the corners
    corner_result = np.where(dst>0.01*dst.max(), dst, 0)
    ### Draws the corners the img copy
    for i in range(0, len(corner_result), 1):
        for j in range(0, len(corner_result[0]), 1):
            if corner_result[i][j]:
                copy[i][j] = [0,0,255]

    return corner_result, copy

def findLines(img):
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray,(5,5),0)
    canny = cv2.Canny(blur, 100, 200, apertureSize=3)
    # Low thresh - start with the possible lines
    # minLineLength - Long enough to be a lane edge
    # maxLineGap - low 10 pixels because we expect the line to be solid
    lines = cv2.HoughLinesP(canny, 1, np.pi/180, threshold=40, minLineLength=200, maxLineGap=15 )

    for x1,y1,x2,y2 in lines[0]:
        cv2.line(img, (x1,y1), (x2,y2), (0,255,0), 2)

    cv2.imshow("canny", canny)

    return lines, img


def detectLane(img, lines, corners):
    """
    Combined the lines and corners to detect a lane
    """
    for i in range(0, len(lines[0]), 1):
        for j in range(0, len(lines[0]), 1):
            if i != j :
                a = Point(lines[0][i][0], lines[0][i][1])
                b = Point(lines[0][i][2], lines[0][i][3])
                c = Point(lines[0][j][0], lines[0][j][1])
                d = Point(lines[0][j][2], lines[0][j][3])
                if(intersect(a,b,c,d)):
                    cv2.line(img, (a.x,a.y), (b.x,b.y), [0,255,0], thickness=2)
                    cv2.line(img, (c.x,c.y), (d.x,d.y), [0,255,0], thickness=2)
    return img


def ccw(A,B,C):
    return (C.y-A.y)*(B.x-A.x) > (B.y-A.y)*(C.x-A.x)

def intersect(A,B,C,D):
    return ccw(A,C,D) != ccw(B,C,D) and ccw(A,B,C) != ccw(A,B,D)

filename = 'pictures/hpl.jpg'
img = cv2.imread(filename)
c_copy = np.copy(img)
l_copy = np.copy(img)
s_copy = np.copy(img)

corners, c_disp = findCorners(c_copy)
lines, l_disp = findLines(l_copy)
s_disp = detectLane(s_copy, lines, corners)
cv2.imshow('original', img)
cv2.imshow('corners',c_disp)
cv2.imshow('hough_lines',l_disp)
cv2.imshow('selected', s_disp)
if cv2.waitKey(0) & 0xff == 27:
    cv2.destroyAllWindows()