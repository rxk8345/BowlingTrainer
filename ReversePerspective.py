import cv2
import numpy as np
from matplotlib import pyplot as plt

"""
    Richy Kapadia
    Idea:
        Test reverse perspective by hard coding lanes edges
"""



def reverse_rerspective( img, left, right ):

    if left is None or right is None:
        return

    x1 = left[0]
    y1 = left[1]
    x2 = left[2]
    y2 = left[3]

    x3 = right[0]
    y3 = right[1]
    x4 = right[2]
    y4 = right[3]

    corners = np.array([[x1, y1], [x2, y2], [x4, y4], [x3, y3]]).astype(np.float32)
    height = 50 * 12
    width = 39 * 5
    quad = np.zeros((width, height))
    quad_pts = np.array([[0, height], [0, 0], [width, 0], [width, height]]).astype(np.float32)
    transMat = cv2.getPerspectiveTransform(corners, quad_pts)
    transformed = cv2.warpPerspective(img, transMat, quad.shape)

    cv2.imshow("transformed", transformed)

    return transformed
