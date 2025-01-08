import React from 'react';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Points from './points';
import Weight from './weight';
import BloodPressure from './blood-pressure';
import Preferences from './preferences';
/* jhipster-needle-add-route-import - JHipster will add routes here */
import { Route } from 'react-router-dom';

export default () => {
    return (
        <div>
            <ErrorBoundaryRoutes>
                <Route path="points/*" element={<Points />} />
                <Route path="weight/*" element={<Weight />} />
                <Route path="blood-pressure/*" element={<BloodPressure />} />
                <Route path="preferences/*" element={<Preferences />} />
                {/* jhipster-needle-add-route-path - JHipster will add routes here */}
            </ErrorBoundaryRoutes>
        </div>
    );
};
