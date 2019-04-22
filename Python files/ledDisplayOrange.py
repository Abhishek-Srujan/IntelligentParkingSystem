from sense_hat import SenseHat
sense = SenseHat()
orange = [255, 128, 0]
pixel_orange = [orange for i in range(64)]
sense.set_pixels(pixel_orange)

