import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import { ASC } from 'app/shared/util/pagination.constants';
import { cleanEntity } from 'app/shared/util/entity-utils';
import { EntityState, IQueryParams, createEntitySlice, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IPreferences, defaultValue } from 'app/shared/model/preferences.model';

const initialState: EntityState<IPreferences> = {
    loading: false,
    errorMessage: null,
    entities: [],
    entity: defaultValue,
    updating: false,
    updateSuccess: false,
};

const apiUrl = 'api/preferences';
const apiSearchUrl = 'api/preferences/_search';

// Actions

export const searchEntities = createAsyncThunk(
    'preferences/search_entity',
    async ({ query, page, size, sort }: IQueryParams) => {
        const requestUrl = `${apiSearchUrl}?query=${query}`;
        return axios.get<IPreferences[]>(requestUrl);
    },
    { serializeError: serializeAxiosError },
);

export const getEntities = createAsyncThunk(
    'preferences/fetch_entity_list',
    async ({ sort }: IQueryParams) => {
        const requestUrl = `${apiUrl}?${sort ? `sort=${sort}&` : ''}cacheBuster=${new Date().getTime()}`;
        return axios.get<IPreferences[]>(requestUrl);
    },
    { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
    'preferences/fetch_entity',
    async (id: string | number) => {
        const requestUrl = `${apiUrl}/${id}`;
        return axios.get<IPreferences>(requestUrl);
    },
    { serializeError: serializeAxiosError },
);

export const createEntity = createAsyncThunk(
    'preferences/create_entity',
    async (entity: IPreferences, thunkAPI) => {
        const result = await axios.post<IPreferences>(apiUrl, cleanEntity(entity));
        thunkAPI.dispatch(getEntities({}));
        return result;
    },
    { serializeError: serializeAxiosError },
);

export const updateEntity = createAsyncThunk(
    'preferences/update_entity',
    async (entity: IPreferences, thunkAPI) => {
        const result = await axios.put<IPreferences>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
        thunkAPI.dispatch(getEntities({}));
        return result;
    },
    { serializeError: serializeAxiosError },
);

export const partialUpdateEntity = createAsyncThunk(
    'preferences/partial_update_entity',
    async (entity: IPreferences, thunkAPI) => {
        const result = await axios.patch<IPreferences>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
        thunkAPI.dispatch(getEntities({}));
        return result;
    },
    { serializeError: serializeAxiosError },
);

export const deleteEntity = createAsyncThunk(
    'preferences/delete_entity',
    async (id: string | number, thunkAPI) => {
        const requestUrl = `${apiUrl}/${id}`;
        const result = await axios.delete<IPreferences>(requestUrl);
        thunkAPI.dispatch(getEntities({}));
        return result;
    },
    { serializeError: serializeAxiosError },
);

// slice

export const PreferencesSlice = createEntitySlice({
    name: 'preferences',
    initialState,
    extraReducers(builder) {
        builder
            .addCase(getEntity.fulfilled, (state, action) => {
                state.loading = false;
                state.entity = action.payload.data;
            })
            .addCase(deleteEntity.fulfilled, state => {
                state.updating = false;
                state.updateSuccess = true;
                state.entity = {};
            })
            .addMatcher(isFulfilled(getEntities, searchEntities), (state, action) => {
                const { data } = action.payload;

                return {
                    ...state,
                    loading: false,
                    entities: data.sort((a, b) => {
                        if (!action.meta?.arg?.sort) {
                            return 1;
                        }
                        const order = action.meta.arg.sort.split(',')[1];
                        const predicate = action.meta.arg.sort.split(',')[0];
                        return order === ASC ? (a[predicate] < b[predicate] ? -1 : 1) : b[predicate] < a[predicate] ? -1 : 1;
                    }),
                };
            })
            .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
                state.updating = false;
                state.loading = false;
                state.updateSuccess = true;
                state.entity = action.payload.data;
            })
            .addMatcher(isPending(getEntities, getEntity, searchEntities), state => {
                state.errorMessage = null;
                state.updateSuccess = false;
                state.loading = true;
            })
            .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
                state.errorMessage = null;
                state.updateSuccess = false;
                state.updating = true;
            });
    },
});

export const { reset } = PreferencesSlice.actions;

// Reducer
export default PreferencesSlice.reducer;
