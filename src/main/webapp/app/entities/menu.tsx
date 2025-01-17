import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { Translate } from 'react-jhipster';

const EntitiesMenu = () => {
    return (
        <>
            <MenuItem icon="asterisk" to="/points">
                <Translate contentKey="global.menu.entities.points" />
            </MenuItem>
            <MenuItem icon="asterisk" to="/weight">
                <Translate contentKey="global.menu.entities.weight" />
            </MenuItem>
            <MenuItem icon="asterisk" to="/blood-pressure">
                <Translate contentKey="global.menu.entities.bloodPressure" />
            </MenuItem>
            <MenuItem icon="asterisk" to="/preferences">
                <Translate contentKey="global.menu.entities.preferences" />
            </MenuItem>
            {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
        </>
    );
};

export default EntitiesMenu;
