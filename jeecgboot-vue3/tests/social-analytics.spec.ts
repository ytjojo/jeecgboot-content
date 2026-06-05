import { SOCIAL_EVENTS, trackSocialEvent } from '../src/utils/social/analytics';

describe('SOCIAL_EVENTS contract', () => {
  it('defines all expected event names', () => {
    expect(SOCIAL_EVENTS.MUTUAL_FOLLOW_BADGE_SHOW).toBe('mutual_follow_badge_show');
    expect(SOCIAL_EVENTS.MUTUAL_FOLLOW_BADGE_CLICK).toBe('mutual_follow_badge_click');
    expect(SOCIAL_EVENTS.MUTUAL_FOLLOW_CANCEL).toBe('mutual_follow_cancel');
    expect(SOCIAL_EVENTS.FAN_LIST_VIEW).toBe('fan_list_view');
    expect(SOCIAL_EVENTS.FAN_TREND_VIEW).toBe('fan_trend_view');
    expect(SOCIAL_EVENTS.FAN_TREND_POINT_CLICK).toBe('fan_trend_point_click');
    expect(SOCIAL_EVENTS.INVITE_CODE_COPY).toBe('invite_code_copy');
    expect(SOCIAL_EVENTS.INVITE_LANDING_PAGE_VIEW).toBe('invite_landing_page_view');
    expect(SOCIAL_EVENTS.INVITE_REGISTER_CLICK).toBe('invite_register_click');
    expect(SOCIAL_EVENTS.INVITE_REGISTER_COMPLETE).toBe('invite_register_complete');
    expect(SOCIAL_EVENTS.INVITE_REWARD_TRIGGER).toBe('invite_reward_trigger');
    expect(SOCIAL_EVENTS.COMMUNITY_ROLE_BADGE_SHOW).toBe('community_role_badge_show');
    expect(SOCIAL_EVENTS.COMMUNITY_ROLE_BADGE_CLICK).toBe('community_role_badge_click');
    expect(SOCIAL_EVENTS.MODERATOR_ACTION_EXECUTE).toBe('moderator_action_execute');
    expect(SOCIAL_EVENTS.PRIVATE_CONTENT_PUBLISH).toBe('private_content_publish');
    expect(SOCIAL_EVENTS.PRIVATE_CONTENT_ACCESS_DENIED).toBe('private_content_access_denied');
  });

  it('uses snake_case naming convention for every event', () => {
    Object.values(SOCIAL_EVENTS).forEach((v) => {
      expect(v).toMatch(/^[a-z_]+$/);
    });
  });

  it('has no duplicate event names', () => {
    const vals = Object.values(SOCIAL_EVENTS);
    expect(new Set(vals).size).toBe(vals.length);
  });

  it('has exactly 16 events', () => {
    expect(Object.keys(SOCIAL_EVENTS)).toHaveLength(16);
  });
});

describe('trackSocialEvent', () => {
  afterEach(() => {
    // Restore window state
    delete (window as any).trackEvent;
    delete (window as any).dataLayer;
  });

  it('calls window.trackEvent when available', () => {
    const mockTrackEvent = jest.fn();
    (window as any).trackEvent = mockTrackEvent;

    trackSocialEvent(SOCIAL_EVENTS.FAN_LIST_VIEW, { userId: '123' });

    expect(mockTrackEvent).toHaveBeenCalledWith('fan_list_view', { userId: '123' });
  });

  it('falls back to dataLayer.push when trackEvent is not available', () => {
    const mockPush = jest.fn();
    (window as any).dataLayer = { push: mockPush };

    trackSocialEvent(SOCIAL_EVENTS.MUTUAL_FOLLOW_CANCEL, { targetUserId: '456' });

    expect(mockPush).toHaveBeenCalledWith({
      event: 'mutual_follow_cancel',
      targetUserId: '456',
    });
  });

  it('falls back to console.debug when neither trackEvent nor dataLayer exist', () => {
    const debugSpy = jest.spyOn(console, 'debug').mockImplementation();

    trackSocialEvent(SOCIAL_EVENTS.INVITE_CODE_COPY, { type: 'code' });

    expect(debugSpy).toHaveBeenCalledWith('[social-analytics]', 'invite_code_copy', { type: 'code' });
    debugSpy.mockRestore();
  });

  it('works without payload', () => {
    const mockTrackEvent = jest.fn();
    (window as any).trackEvent = mockTrackEvent;

    trackSocialEvent(SOCIAL_EVENTS.FAN_LIST_VIEW);

    expect(mockTrackEvent).toHaveBeenCalledWith('fan_list_view', undefined);
  });

  it('silently handles errors', () => {
    (window as any).trackEvent = () => {
      throw new Error('test');
    };
    const debugSpy = jest.spyOn(console, 'debug').mockImplementation();

    // Should not throw
    expect(() => trackSocialEvent(SOCIAL_EVENTS.FAN_LIST_VIEW)).not.toThrow();
    debugSpy.mockRestore();
  });
});
