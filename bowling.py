import numpy as np
import cv2

def parallel(a, b):
    return a.slope == b.slope

def intersect(a, b):
    """
    Calculates the intersection between two lines.
    :param a: First line
    :param b: Second line
    :return: Intersection point
    """
    if parallel(a, b):
        return None
    else:
        return ((b.intercept - a.intercept) / (a.slope - b.slope), (a.slope * b.intercept - b.slope * a.intercept) / (a.slope - b.slope))

def angle(a, b):
    """
    Calculates the acute angle between two lines (if one exists).
    :param a:
    :param b:
    :return: Angle in radians
    """
    if parallel(a, b):
        return -np.pi
    else:
        angleA = np.arctan(a.slope)
        angleB = np.arctan(b.slope)
        diff = np.abs(angleA - angleB)
        if diff > (np.pi / 2):
            return np.pi - diff
        else:
            return diff


class Line:
    def __init__(self, slope, intercept):
        self.slope = slope
        self.intercept = intercept

    def y(self, x):
        return self.slope * x + self.intercept

    def x(self, y):
        return (y - self.intercept) / self.slope

    def __str__(self):
        return "(Slope: " + str(self.slope) + ", Intercept: " + str(self.intercept) + ")"

if __name__ == "__main__":
    image = cv2.imread("./Testing Data/lane.jpg")
    # image = cv2.resize(image, (int(image.shape[1] / 2), int(image.shape[0] / 2)))
    x = image.shape[1]
    y = image.shape[0]

    # z = image.reshape((-1, 3))
    z = image.reshape((-1, 3))
    z = np.float32(z)
    criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 10, 1.0)
    k = 2
    ret, label, center = cv2.kmeans(z, k, None, criteria, 10, cv2.KMEANS_RANDOM_CENTERS)

    center = np.uint8(center)
    res = center[label.flatten()]
    # res2 = res.reshape(image.shape)
    res2 = res.reshape(image.shape)

    gray = cv2.cvtColor(res2, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray, (3, 3), 100)
    # look into mean blurring
    canny = cv2.Canny(blur, 50, 150, apertureSize=3)
    lines = cv2.HoughLines(canny, 1, np.pi / 180, 200)

    # sift = cv2.xfeatures2d.SIFT_create()
    # kp = sift.detect(gray, None)
    #
    # cv2.drawKeypoints(gray, kp, res2)
    #
    # grayf = np.float32(gray)
    # dest = cv2.cornerHarris(grayf, 2, 3, 0.04)
    # dest = cv2.dilate(dest, None)
    # image[dest > 0.01 * dest.max()] = [0, 0, 255]

    horiz = []
    lin = []

    for rho, theta in lines[:, 0]:
        a = np.cos(theta)
        b = np.sin(theta)
        x0 = a * rho
        y0 = b * rho
        x1 = int(x0 + 1000 * (-b))
        y1 = int(y0 + 1000 * a)
        x2 = int(x0 - 1000 * (-b))
        y2 = int(y0 - 1000 * a)

        if (x2 - x1) == 0:
            continue
        slope = (y2 - y1) / (x2 - x1)
        intercept = y1 - slope * x1
        int2 = y2 - slope * x2

        line = Line(slope, intercept)

        if np.abs(slope) > 0.1:
            lin.append(line)
            cv2.line(res2, (int(line.x(0)), 0), (int(line.x(y)), y), (0, 0, 255), 2)
        else:
            pa = (0, int(line.y(0)))
            pb = (x, int(line.y(x)))

            if (pa[1] < y / 2 and pb[1] < y / 2):
                pass
            else:
                horiz.append(line)
    slope_avg = np.average([line.slope for line in horiz])
    inter_avg = np.average([line.intercept for line in horiz])

    s = sorted(lin, key=lambda line: line.x(y * 4))
    draw = []
    for i in range(len(s) - 1):
        deg = np.degrees(angle(s[i], s[i + 1]))
        if deg > 20:
            print("drawn: ", deg)
            draw.append(s[i])
            draw.append(s[i + 1])

    for line in draw:
        cv2.line(res2, (int(line.x(0)), 0), (int(line.x(y)), y), (255, 0, 0), 2)

    if (slope_avg and inter_avg):
        rawr = Line(slope_avg, inter_avg)
        cv2.line(res2, (0, int(rawr.y(0))), (x, int(rawr.y(x))), (0, 255, 0), 2)


    cv2.imshow("image", image)
    cv2.imshow("means", res2)
    cv2.imshow("canny", canny)
    # cv2.imshow("gray", gray)
    # cv2.imshow("blur", blur)
    key = cv2.waitKey(0)
    # print("Key pressed")