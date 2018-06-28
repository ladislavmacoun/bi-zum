import os
import re
import PIL
import scipy
import scipy.ndimage
import numpy as np

class Image(object):
    def __init__(self, datafname, samplefname):
        #ndarray
        data_array = scipy.ndimage.imread("data/" + datafname)
        data_mask = scipy.ndimage.imread("samples/" + samplefname)

        #print("Data array: {0}".format(data_array))
        #print("Data mask: {0}".format(data_mask))

        dist = getDist()

    def getDist():
        for byte in byte_array:
        

class ImageClassification(object):
    def __init__(self) :
        dataset = self.loadData()
        images = []
        for item in dataset:
            images.append(Image(dataset[item].get("data"), dataset[item].get("sample")))
        classes = {"A" : [], "B" : []}

    def loadData(self):
        dataset = {}
        for filename in os.listdir('data'):
            dataset[re.findall('\d+', filename)[0]] = {"data" : filename}

        for filename in os.listdir('samples'):
            dataset[re.findall('\d+', filename)[0]].update({"sample" : filename})
            
        return dataset


def classify(sample):
    return

def getDist(data, sample):
    return


def kNN(dataset, samples):
    for item in range(dataset):
        getDist(dataset[item], samples[item])
        classify(dist, samples[item])

imgclass = ImageClassification()
imgclass.loadData()
