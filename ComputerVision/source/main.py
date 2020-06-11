
import sys
import json
import time
import base64
import io
import cv2

from PIL import Image

from message import Message
from connector import Connector
from detector import Detector

connector = None
detector = None

def main():
    global connector
    global detector

    # Get port argument
    if len(sys.argv) < 2:
        raise Exception("Missing port argument")
    port = None
    try:
        port = int(sys.argv[1])
        if type(port) is not int or port < 1 or port > 65535:
            raise Exception()
    except:
        raise TypeError("Port argument must be an integer between 1 and 65535 (inclusive)")

    # Connect to server
    connector = Connector(port, message_received)
    
    # Start Detector
    detector = Detector(detection_started, new_detection)

    print("Started camera")
    
    # TODO: FIx this
    detector._thread.join()

    


def new_detection():
    pass
    # Send message to saserver


def detection_started():
    send_image()    


def message_received(msg: Message):

    # Start client
    if msg._code is 200:
        print("Client was told to start, but it hasn't been implemented yet")
    
    # Request image
    if msg._code is 201:
        send_image()



def send_image():
    global connector
    global detector
    image = detector.get_latest_frame()
    if image is not None:
        # First encode into base64 bytes, and then into ascii string
        encodedImage = base64.b64encode(image).decode('ascii')
        connector.send_message(Message(
            102,
            json.dumps(
                {"image": encodedImage, "width":1280, "height":720}
        )), True)
    else:
        print("WARNING: Image was None!")    


if __name__ == "__main__":
    main()