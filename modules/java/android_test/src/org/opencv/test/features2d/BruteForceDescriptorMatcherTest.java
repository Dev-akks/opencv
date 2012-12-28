package org.opencv.test.features2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.test.OpenCVTestCase;
import org.opencv.test.OpenCVTestRunner;

public class BruteForceDescriptorMatcherTest extends OpenCVTestCase {

    DescriptorMatcher matcher;
    int matSize;
    DMatch[] truth;

    private Mat getMaskImg() {
        return new Mat(5, 2, CvType.CV_8U, new Scalar(0)) {
            {
                put(0, 0, 1, 1, 1, 1);
            }
        };
    }

    private Mat getQueryDescriptors() {
        Mat img = getQueryImg();
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);

        String filename = OpenCVTestRunner.getTempFileName("yml");
        writeFile(filename, "%YAML:1.0\nhessianThreshold: 8000.\noctaves: 3\noctaveLayers: 4\nupright: 0\n");
        detector.read(filename);

        detector.detect(img, keypoints);
        extractor.compute(img, keypoints, descriptors);

        return descriptors;
    }

    private Mat getQueryImg() {
        Mat cross = new Mat(matSize, matSize, CvType.CV_8U, new Scalar(255));
        Core.line(cross, new Point(30, matSize / 2), new Point(matSize - 31, matSize / 2), new Scalar(100), 3);
        Core.line(cross, new Point(matSize / 2, 30), new Point(matSize / 2, matSize - 31), new Scalar(100), 3);

        return cross;
    }

    private Mat getTrainDescriptors() {
        Mat img = getTrainImg();
        MatOfKeyPoint keypoints = new MatOfKeyPoint(new KeyPoint(50, 50, 16, 0, 20000, 1, -1), new KeyPoint(42, 42, 16, 160, 10000, 1, -1));
        Mat descriptors = new Mat();

        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);

        extractor.compute(img, keypoints, descriptors);

        return descriptors;
    }

    private Mat getTrainImg() {
        Mat cross = new Mat(matSize, matSize, CvType.CV_8U, new Scalar(255));
        Core.line(cross, new Point(20, matSize / 2), new Point(matSize - 21, matSize / 2), new Scalar(100), 2);
        Core.line(cross, new Point(matSize / 2, 20), new Point(matSize / 2, matSize - 21), new Scalar(100), 2);

        return cross;
    }

    protected void setUp() throws Exception {
        super.setUp();
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        matSize = 100;

        truth = new DMatch[] {
                new DMatch(0, 0, 0, 1.0496940f),
                new DMatch(1, 0, 0, 1.0984558f),
                new DMatch(2, 1, 0, 0.4945875f),
                new DMatch(3, 1, 0, 0.48435235f),
                new DMatch(4, 0, 0, 1.0836693f)
                };
    }

    public void testAdd() {
        matcher.add(Arrays.asList(new Mat()));
        assertFalse(matcher.empty());
    }

    public void testClear() {
        matcher.add(Arrays.asList(new Mat()));

        matcher.clear();

        assertTrue(matcher.empty());
    }

    public void testClone() {
        Mat train = new Mat(1, 1, CvType.CV_8U, new Scalar(123));
        Mat truth = train.clone();
        matcher.add(Arrays.asList(train));

        DescriptorMatcher cloned = matcher.clone();

        assertNotNull(cloned);

        List<Mat> descriptors = cloned.getTrainDescriptors();
        assertEquals(1, descriptors.size());
        assertMatEqual(truth, descriptors.get(0));
    }

    public void testCloneBoolean() {
        matcher.add(Arrays.asList(new Mat()));

        DescriptorMatcher cloned = matcher.clone(true);

        assertNotNull(cloned);
        assertTrue(cloned.empty());
    }

    public void testCreate() {
        assertNotNull(matcher);
    }

    public void testEmpty() {
        assertTrue(matcher.empty());
    }

    public void testGetTrainDescriptors() {
        Mat train = new Mat(1, 1, CvType.CV_8U, new Scalar(123));
        Mat truth = train.clone();
        matcher.add(Arrays.asList(train));

        List<Mat> descriptors = matcher.getTrainDescriptors();

        assertEquals(1, descriptors.size());
        assertMatEqual(truth, descriptors.get(0));
    }

    public void testIsMaskSupported() {
        assertTrue(matcher.isMaskSupported());
    }

    public void testKnnMatchMatListOfListOfDMatchInt() {
        fail("Not yet implemented");
    }

    public void testKnnMatchMatListOfListOfDMatchIntListOfMat() {
        fail("Not yet implemented");
    }

    public void testKnnMatchMatListOfListOfDMatchIntListOfMatBoolean() {
        fail("Not yet implemented");
    }

    public void testKnnMatchMatMatListOfListOfDMatchInt() {
        final int k = 3;
        Mat train = getTrainDescriptors();
        Mat query = getQueryDescriptors();
        List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        matcher.knnMatch(query, train, matches, k);
        /*
        Log.d("knnMatch", "train = " + train);
        Log.d("knnMatch", "query = " + query);

        matcher.add(train);
        matcher.knnMatch(query, matches, k);
        */
        assertEquals(query.rows(), matches.size());
        for(int i = 0; i<matches.size(); i++)
        {
            MatOfDMatch vdm = matches.get(i);
            //Log.d("knn", "vdm["+i+"]="+vdm.dump());
            assertTrue(Math.min(k, train.rows()) >= vdm.total());
            for(DMatch dm : vdm.toArray())
            {
                assertEquals(dm.queryIdx, i);
            }
        }
    }

    public void testKnnMatchMatMatListOfListOfDMatchIntMat() {
        fail("Not yet implemented");
    }

    public void testKnnMatchMatMatListOfListOfDMatchIntMatBoolean() {
        fail("Not yet implemented");
    }

    public void testMatchMatListOfDMatch() {
        Mat train = getTrainDescriptors();
        Mat query = getQueryDescriptors();
        MatOfDMatch matches = new MatOfDMatch();
        matcher.add(Arrays.asList(train));

        matcher.match(query, matches);

        assertArrayDMatchEquals(truth, matches.toArray(), EPS);
    }

    public void testMatchMatListOfDMatchListOfMat() {
        Mat train = getTrainDescriptors();
        Mat query = getQueryDescriptors();
        Mat mask = getMaskImg();
        MatOfDMatch matches = new MatOfDMatch();
        matcher.add(Arrays.asList(train));

        matcher.match(query, matches, Arrays.asList(mask));

        assertListDMatchEquals(Arrays.asList(truth[0], truth[1]), matches.toList(), EPS);
    }

    public void testMatchMatMatListOfDMatch() {
        Mat train = getTrainDescriptors();
        Mat query = getQueryDescriptors();
        MatOfDMatch matches = new MatOfDMatch();

        matcher.match(query, train, matches);

        assertArrayDMatchEquals(truth, matches.toArray(), EPS);

        // OpenCVTestRunner.Log("matches found: " + matches.size());
        // for (DMatch m : matches)
        // OpenCVTestRunner.Log(m.toString());
    }

    public void testMatchMatMatListOfDMatchMat() {
        Mat train = getTrainDescriptors();
        Mat query = getQueryDescriptors();
        Mat mask = getMaskImg();
        MatOfDMatch matches = new MatOfDMatch();

        matcher.match(query, train, matches, mask);

        assertListDMatchEquals(Arrays.asList(truth[0], truth[1]), matches.toList(), EPS);
    }

    public void testRadiusMatchMatListOfListOfDMatchFloat() {
        fail("Not yet implemented");
    }

    public void testRadiusMatchMatListOfListOfDMatchFloatListOfMat() {
        fail("Not yet implemented");
    }

    public void testRadiusMatchMatListOfListOfDMatchFloatListOfMatBoolean() {
        fail("Not yet implemented");
    }

    public void testRadiusMatchMatMatListOfListOfDMatchFloat() {
        fail("Not yet implemented");
    }

    public void testRadiusMatchMatMatListOfListOfDMatchFloatMat() {
        fail("Not yet implemented");
    }

    public void testRadiusMatchMatMatListOfListOfDMatchFloatMatBoolean() {
        fail("Not yet implemented");
    }

    public void testRead() {
        String filename = OpenCVTestRunner.getTempFileName("yml");
        writeFile(filename, "%YAML:1.0\n");

        matcher.read(filename);
        assertTrue(true);// BruteforceMatcher has no settings
    }

    public void testTrain() {
        matcher.train();// BruteforceMatcher does not need to train
    }

    public void testWrite() {
        String filename = OpenCVTestRunner.getTempFileName("yml");

        matcher.write(filename);

        String truth = "%YAML:1.0\n";
        assertEquals(truth, readFile(filename));
    }

}
