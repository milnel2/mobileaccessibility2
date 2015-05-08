Street Sign Reader README:

The Mandroids : Shurui Sun, Jon Luo, Gary Kuo

The following diagram lists the all the classes and where they are used. 
Preview, DrawOnTop, and DoServerOCR are not Activities. Going down equates
to progressing through our application.


StreetSignReader --> (Preview + DrawOnTop)

								|
								|
								V
						
							  Touch --> (DoServerOCR) 
							  
							  				|
							  				|
							  				V
						   			   DisplayResult

					  
At any point, the the user may swipe (in any direction) to
go back to the previous activity.

--------------

Here's a brief, high-level list of what each class does.

* StreetSignReader *
Starts the camera and takes the photo.

* Preview *
Implements the preview of the camera.

* DrawOnTop *
Draws the lines on the preview screen.

* Touch *
Contains all of our image manipulation code
(pinch to zoom, monochromatic adjustment, etc)
Sends and receives the OCR result.

* DoServerOCR *
Provides the actual sending of the image to the Tesseract server.

* DisplayResult *
Contains a TextView of the OCR result.
(There's room for additional logic such as GPS here, 
it's not included since GPS proved to be unreliable in testing.)

* ImagePreview is totally depreciated and not used at all.
 