package com.ioncannon.cpuburn.gpugflops;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class BurnFragment extends Fragment {
    private LineChartView chart = null;
    private OnFragmentInteractionListener mListener;
    private View view = null;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void testChart() {
        List values = new ArrayList();
        float[] k = new float[]{0.43697f, 0.43823f, 0.43423f, 0.44034f, 0.43907f, 0.44539f, 0.44613f, 0.45802f, 0.47433f, 0.51127f, 0.58242f, 0.70134f, 0.90962f, 1.20861f, 1.56812f, 1.88838f, 2.11444f, 2.26357f, 2.38271f, 2.4589f, 2.51373f, 2.55688f, 2.58688f, 2.60866f, 2.62392f, 2.6294f, 2.63803f, 2.64497f, 2.64802f, 2.65181f, 2.65066f, 2.66034f, 2.65918f, 2.66223f, 2.66497f, 2.66981f};
        for (int i = 0; i < k.length; i++) {
            values.add(new PointValue((float) (i + 1), k[i]));
        }
        Line line = new Line(values).setColor(-16776961).setCubic(true);
        List<Line> lines = new ArrayList();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setAxisXBottom(new Axis());
        data.setAxisYLeft(new Axis());
        this.chart.setLineChartData(data);
    }

    public static BurnFragment newInstance() {
        return new BurnFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_burn, container, false);
        this.chart = (LineChartView) this.view.findViewById(R.id.chartResultDebug);
        testChart();
        return this.view;
    }

    public void onButtonPressed(Uri uri) {
        if (this.mListener != null) {
            this.mListener.onFragmentInteraction(uri);
        }
    }
}
