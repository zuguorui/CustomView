#Custom View
- This is a project that include some custom view for Android. I will always create some practice work in this project. All the custom view classes are in `com.zu.customview.view`. Here is some introduction.

####1. Slide menu layout
This is a slide menu. User can fling from the left of screen to right to show the menu. This layout can notify listeners the progress of sliding so that you can make some action to the slide, for example, rotate a indicator.

####2. ViewPagerIndicator
Android dose not have an good indicator for ViewPager. Though it is not difficult to piece some view together to achieve the gole, it is troublesome to do that every time when you need a indicator. This Indicator is very easy to use. Just add tag to it, and call `public void listen(int position, float positionOffset, int positionOffsetPixels)` in ViewPager's callback. Then it can monitor the scroll of ViewPager. You can also custom the color of indicator line and selected item, space between tags. What's more, you can set the layout mode in `balance` or not. When you have little tags that they can not full the width and you want them were layouted in balance, you can set balance mode. If you have so many tags that you want them to be layouted one follows another, you can cancel balance mode.

####3. ZoomLayout
This is like a grid view but it can zoom child views. This view is very difficult to create. If you really need this, you can read the source code. The method to use it is the same as LietView.

