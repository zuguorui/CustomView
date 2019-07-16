# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

file(GLOB CPP_FILES "src/main/cpp/*.cpp")

file(GLOB C_FILES "src/main/cpp/*.c")

include_directories(src/main/cpp)

add_library(
	native-lib
	SHARED
	${CPP_FILES}
	${C_FILES}
)

target_link_libraries(
    native-lib
    log
    android
    OpenSLES
)