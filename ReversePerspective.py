import cv2
import numpy as np
from matplotlib import pyplot as plt

"""
    Richy Kapadia
    Idea:
        Test reverse perspective by hard coding lanes edges
"""



if __name__ == '__main__':
    filename = 'pictures/hpl_pt.jpg'
    filename = 'pictures/pro_anvilane.jpg'

    img = cv2.imread(filename)
    org = np.copy(img)

    # bottom left ---- top left
    # x1, y1, x2, y2 = [316, 356, 352, 134]
    x1, y1, x2, y2 = [200, 238, 523, 48]
    # bottom right ---- top right
    # x3, y3, x4, y4 = [492, 367, 395, 134]
    x3, y3, x4, y4 = [457, 282, 555, 50]

    # line 1 left line
    cv2.line(img, (x1, y1), (x2, y2), (0,255,0), 2)

    # line 2 right line
    cv2.line(img, (x3, y3), (x4, y4), (0,255,0), 2)

    # line 3 bottom
    cv2.line(img, (x1, y1), (x3, y3), (0,255,0), 2)

    # line 4 top
    cv2.line(img, (x2, y2), (x4, y4), (0,255,0), 2)

    corners = np.array([[x1, y1], [x2, y2], [x4, y4], [x3, y3]]).astype(np.float32)
    height = 50 * 12
    width = 39 * 5
    quad = np.zeros((width, height))
    quad_pts = np.array([[0, height], [0, 0], [width, 0], [width, height]]).astype(np.float32)
    transMat = cv2.getPerspectiveTransform(corners, quad_pts)
    transformed = cv2.warpPerspective(org, transMat, quad.shape)


    plt.imshow(img)
    plt.show()
    cv2.imshow("transformed", transformed)
    cv2.waitKey(0)