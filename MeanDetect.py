import cv2
import numpy as np

"""
    put all angles in a dictionary (by rounded angle)
    choose bottom pt and top pt from each angle group
"""
def detect(img):
    height = img.shape[0]
    factor = height / 150
    width = img.shape[1]

    height = height / factor
    width = width / factor

    scaled = cv2.resize(img, (width, height))
    disp = np.copy(scaled)
    blurred = cv2.bilateralFilter(scaled, 3, 100, 1)

    mean = kmean(blurred, 2)
    cv2.imshow("mean 2", mean)

    gmean = cv2.cvtColor(mean, cv2.COLOR_BGR2GRAY)
    ret, thresh = cv2.threshold(gmean,0,255,cv2.THRESH_OTSU)
    cv2.imshow("thresh", thresh);




    # hsv = cv2.cvtColor(b4, cv2.COLOR_BGR2HSV)
    # t = np.min(hsv[2]) + 1
    # for i in range(0, 25, 1):
    #     t = i * 10
    #     thresh = cv2.inRange(hsv, (0,0,t), (255,255,255))
    #     cv2.imshow("thresh", thresh)
    #     cv2.waitKey(500)

    # gray = cv2.cvtColor(b4, cv2.COLOR_BGR2GRAY)


    edges = cv2.Canny(gmean, 100, 120)
    cv2.imshow("canny", edges)
    lines = cv2.HoughLinesP(edges, 1, np.pi/180, 40, minLineLength=40, maxLineGap=3);

    if lines is not None:
        for x1, y1, x2, y2 in lines[0]:
            cv2.line(disp, (x1,y1), (x2,y2), (0,255,0))

    cv2.imshow("disp", disp)
    return


def kmean(img, k):
    Z = img.reshape((-1,3))
    Z = np.float32(Z)
    criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 10, 1.0)
    ret,label,center=cv2.kmeans(Z, k, criteria, 10, cv2.KMEANS_RANDOM_CENTERS)
    print ret
    print label

    center = np.uint8(center)
    res = center[label.flatten()]
    res2 = res.reshape(img.shape)
    return res2