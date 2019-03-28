import Vue from 'vue'
import Router from 'vue-router'

import Start from './views/Start.vue'

//Welcome modole
import AbstractWelcome from './views/Welcome/AbstractWelcome.vue'
import Tour from './views/Welcome/Tour.vue'
import Choose from './views/Welcome/Choose.vue'

//Welcome modole
import AbstractLogin from './views/Login/AbstractLogin.vue'
import Pincode from './views/Login/Pincode.vue'
import Locked from './views/Login/Locked.vue'
import WaitLocked from './views/Login/WaitLocked.vue'
import AddDevice from './views/Login/AddDevice.vue'

    //Welcome -> recover submodule
    import AbstractRecoverAccount from './views/Login/RecoverAccount/AbstractRecoverAccount.vue'
    import PhoneNumber from './views/Login/RecoverAccount/PhoneNumber.vue'
    import RecoverSms from './views/Login/RecoverAccount/RecoverSms.vue'

//Signup modole
import AbstractSignUp from './views/Signup/AbstractSignUp.vue'
import Names from './views/Signup/Names.vue'
import AccountInfo from './views/Signup/AccountInfo.vue'
import SmsValidation from './views/Signup/SmsValidation.vue'

    //Signup -> pincode submodule
    import AbstractSetPinCode from './views/Signup/Pincode/AbstractSetPinCode.vue'
    import SetPinCode from './views/Signup/Pincode/SetPinCode.vue'
    import ConfirmPinCode from './views/Signup/Pincode/ConfirmPinCode.vue'

//Me module
import AbstractMe from './views/Me/AbstractMe.vue'
import Me from './views/Me/Me.vue'
import Profile from './views/Me/Profile.vue'
import Notifications from './views/Me/Notifications.vue'

//Friends module
import AbstractFriends from './views/Friends/AbstractFriends.vue'
import TimeLine from './views/Friends/TimeLine.vue'
import Overview from './views/Friends/Overview.vue'
import FriendAdd from './views/Friends/Add.vue'

    //Submodule Friends -> Friend
    import AbstractFriend from './views/Friends/Friend/AbstractFriend.vue'
    import Statistics from './views/Friends/Friend/Statistics.vue'
    import FriendsProfile from './views/Friends/Friend/Profile.vue'

//Challenges module
import AbstractChallenges from './views/Challenges/AbstractChallenges.vue'
import ChallengesTabs from './views/Challenges/ChallengesTabs.vue'

    //Submodule Challenges -> TimeBuckets
    import AbstractTimeBuckets from './views/Challenges/TimeBuckets/AbstractTimeBuckets.vue'
    import TimeBucketsList from './views/Challenges/TimeBuckets/TimeBucketsList.vue'


        //Submodule Challenges -> TimeBucket -> TimeBucketAdd
        import AbstractTimeBucketAdd from './views/Challenges/TimeBuckets/TimeBucketAdd/AbstractTimeBucketAdd.vue'
        import TimeBucketsAdd from './views/Challenges/TimeBuckets/TimeBucketAdd/TimeBucketsAdd.vue'
        import TimeBucketsCredit from './views/Challenges/TimeBuckets/TimeBucketAdd/TimeBucketsCredit.vue'

    //Submodule Challenges -> TimeFrames
    import AbstractTimeFrames from './views/Challenges/TimeFrames/AbstractTimeFrames.vue'
    import TimeFramesList from './views/Challenges/TimeFrames/TimeFramesList.vue'

        //Submodule Challenges -> TimeFrame -> TimeFrameAdd
        import AbstractTimeFrameAdd from './views/Challenges/TimeFrames/TimeFrameAdd/AbstractTimeFrameAdd.vue'
        import TimeFrameAdd from './views/Challenges/TimeFrames/TimeFrameAdd/TimeFrameAdd.vue'
        import TimeFrameRange from './views/Challenges/TimeFrames/TimeFrameAdd/TimeFrameRange.vue'

    //Submodule Challenges -> NoGo's
    import AbstractNoGos from './views/Challenges/NoGos/AbstractNoGos.vue'
    import NoGosList from './views/Challenges/NoGos/NoGosList.vue'

        //Submodule Challenges -> NoGo's -> NoGoAdd
        import AbstractNoGoAdd from './views/Challenges/NoGos/NoGoAdd/AbstractNoGoAdd.vue'
        import NoGoAdd from './views/Challenges/NoGos/NoGoAdd/NoGoAdd.vue'

//Settings module
import AbstractSettings from './views/Settings/AbstractSettings.vue'
import Settings from './views/Settings/Settings.vue'

    import AbstractSettingsOverview from './views/Settings/SettingsOverview/AbstractSettingsOverview.vue'
        import SettingsOverview from './views/Settings/SettingsOverview/SettingsOverview.vue'
        import ChangePinCode from './views/Settings/SettingsOverview/ChangePinCode.vue'
        import SettingsAddDevice from './views/Settings/SettingsOverview/AddDevice.vue'
        import Unsubscribe from './views/Settings/SettingsOverview/Unsubscribe.vue'

    import About from './views/Settings/About/About.vue'
    import Help from './views/Settings/Help/Help.vue'

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            name: 'Start',
            component: Start
        },
        {
            path: '/welcome',
            component: AbstractWelcome,
            children: [
                {
                    path: 'tour',
                    name: 'Tour',
                    component: Tour
                },
                {
                    path: 'choose',
                    name: 'Choose',
                    component: Choose
                }
            ]
        },
        {
            path: '/auth',
            component: AbstractLogin,
            children: [
                {
                    path: 'pincode',
                    name: 'Pincode',
                    component: Pincode
                },
                {
                    path: 'locked',
                    name: 'Locked',
                    component: Locked
                },
                {
                    path: 'wait',
                    name: 'WaitLocked',
                    component: WaitLocked
                },
                {
                    path: 'add',
                    name: 'AddDevice',
                    component: AddDevice
                },
                {
                    path: 'recover',
                    component: AbstractRecoverAccount,
                    children: [
                        {
                            path: '/',
                            name: 'PhoneNumber',
                            component: PhoneNumber
                        },
                        {
                            path: 'sms',
                            name: 'RecoverSms',
                            component: RecoverSms
                        }
                    ]
                }
            ]
        },
        {
            path: '/signup',
            component: AbstractSignUp,
            children: [
                {
                    path: 'names',
                    name: 'Names',
                    component: Names
                },
                {
                    path: 'accountinfo',
                    name: 'AccountInfo',
                    component: AccountInfo
                },
                {
                    path: 'validation',
                    name: 'SmsValidation',
                    component: SmsValidation
                },
                {
                    path: 'pincode',
                    component: AbstractSetPinCode,
                    children: [
                        {
                            path: '/',
                            name: 'SetPinCode',
                            component: SetPinCode
                        },
                        {
                            path: 'confirm',
                            name: 'ConfirmPinCode',
                            component: ConfirmPinCode
                        }
                    ]
                }
            ]
        },
        {
            path: '/me',
            component: AbstractMe,
            children: [
                {
                    path: '',
                    name: 'Me',
                    component: Me
                },
                {
                    path: 'profile',
                    name: 'Profile',
                    component: Profile
                },
                {
                    path: 'notifications',
                    name: 'Notifications',
                    component: Notifications
                }
            ]
        },
        {
            path: '/friends',
            component: AbstractFriends,
            children: [
                {
                    path: '',
                    name: 'TimeLine',
                    component: TimeLine
                },
                {
                    path: 'overview',
                    name: 'Overview',
                    component: Overview
                },
                {
                    path: 'add',
                    name: 'FriendAdd',
                    component: FriendAdd
                },
                {
                    path: ':id',
                    component: AbstractFriend,
                    children: [
                        {
                            path: 'statistics',
                            name: 'Statistics',
                            component: Statistics
                        },
                        {
                            path: 'profile',
                            name: 'FriendsProfile',
                            component: FriendsProfile
                        }
                    ]
                }
            ]
        },
        {
            path: '/challenges',
            component: AbstractChallenges,
            children: [
                {
                    path: '',
                    name: 'ChallengesTabs',
                    component: ChallengesTabs,
                    children: [
                        {
                            path: 'timebuckets',
                            component: AbstractTimeBuckets,
                            children: [
                                {
                                    path: '',
                                    name: 'TimeBucketsList',
                                    component: TimeBucketsList
                                },
                                {
                                    path: 'add',
                                    component: AbstractTimeBucketAdd,
                                    children: [
                                        {
                                            path: '',
                                            name: 'TimeBucketsAdd',
                                            component: TimeBucketsAdd
                                        },
                                        {
                                            path: 'credit',
                                            name: 'TimeBucketsCredit',
                                            component: TimeBucketsCredit
                                        }
                                    ]
                                },
                            ]
                        },
                        {
                            path: 'timeframes',
                            component: AbstractTimeFrames,
                            children: [
                                {
                                    path: '',
                                    name: 'TimeFramesList',
                                    component: TimeFramesList
                                },
                                {
                                    path: 'add',
                                    component: AbstractTimeFrameAdd,
                                    children: [
                                        {
                                            path: '',
                                            name: 'TimeFrameAdd',
                                            component: TimeFrameAdd
                                        },
                                        {
                                            path: 'timerange',
                                            name: 'TimeFrameRange',
                                            component: TimeFrameRange
                                        }
                                    ]
                                },
                            ]
                        },
                        {
                            path: 'nogos',
                            component: AbstractNoGos,
                            children: [
                                {
                                    path: '',
                                    name: 'NoGosList',
                                    component: NoGosList
                                },
                                {
                                    path: 'add',
                                    component: AbstractNoGoAdd,
                                    children: [
                                        {
                                            path: '',
                                            name: 'NoGoAdd',
                                            component: NoGoAdd
                                        }
                                    ]
                                },
                            ]
                        }
                    ]
                }
            ]
        },
        {
            path: '/settings',
            component: AbstractSettings,
            children: [
                {
                    path: '',
                    name: 'Settings',
                    component: Settings
                },
                {
                    path: 'overview',
                    component: AbstractSettingsOverview,
                    children: [
                        {
                            path: '',
                            name: 'SettingsOverview',
                            component: SettingsOverview
                        },
                        {
                            path: 'changepin',
                            name: 'ChangePinCode',
                            component: ChangePinCode
                        },
                        {
                            path: 'adddevice',
                            name: 'SettingsAddDevice',
                            component: SettingsAddDevice
                        },
                        {
                            path: 'unsubscribe',
                            name: 'Unsubscribe',
                            component: Unsubscribe
                        }
                    ]
                },
                {
                    path: 'about',
                    name: 'About',
                    component: About
                },
                {
                    path: 'help',
                    name: 'Help',
                    component: Help
                }
            ]
        },
    ]
})
