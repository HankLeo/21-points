import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface IBloodPressure {
    id?: number;
    timestamp?: dayjs.Dayjs;
    systolic?: number;
    diastolic?: number;
    user?: IUser | null;
}

export const defaultValue: Readonly<IBloodPressure> = {};
