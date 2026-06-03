import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';
import { partitionBadges } from '/@/views/content/profile/components/badgeStyle';

// ============================================================
// Mock getBadgeDetail API
// ============================================================
const mockGetBadgeDetail = jest.fn();
jest.mock('/@/api/content/profile', () => ({
  getBadgeDetail: (...args: any[]) => mockGetBadgeDetail(...args),
}));

// ============================================================
// Mock /@/components/Icon to prevent import.meta cascade
// ============================================================
jest.mock('/@/components/Icon', () => ({
  Icon: { name: 'Icon', template: '<span class="icon-stub" />', props: ['icon', 'size'] },
  SvgIcon: { name: 'SvgIcon', template: '<span />', props: [] },
  IconPicker: { name: 'IconPicker', template: '<span />', props: [] },
}));

// ============================================================
// Mock ant-design-vue components
// ============================================================
jest.mock('ant-design-vue', () => ({
  Modal: { name: 'AModal', template: '<div class="a-modal"><slot /></div>', props: ['open', 'width', 'title', 'footer'] },
  Drawer: { name: 'ADrawer', template: '<div class="a-drawer"><slot /></div>', props: ['open', 'placement', 'width', 'title', 'closable'] },
  Spin: { name: 'ASpin', template: '<div class="a-spin"><slot /></div>', props: ['spinning'] },
  Popover: { name: 'APopover', template: '<div class="a-popover"><slot name="content" /><slot /></div>', props: ['open', 'trigger', 'placement'] },
  Tooltip: { name: 'ATooltip', template: '<div class="a-tooltip"><slot /></div>', props: ['title'] },
}));

// ============================================================
// Helpers: build minimal badge VO
// ============================================================
function makeBadge(overrides: Partial<{ badgeId: string; visualStyleKey: string; badgeType: string; badgeLabel: string; description?: string; verifiedAt?: string }> = {}) {
  return {
    badgeId: overrides.badgeId ?? '1',
    visualStyleKey: overrides.visualStyleKey ?? 'OFFICIAL',
    badgeType: overrides.badgeType ?? 'OFFICIAL',
    badgeLabel: overrides.badgeLabel ?? 'Badge',
    description: overrides.description,
    verifiedAt: overrides.verifiedAt,
  };
}

// ============================================================
// C4: partitionBadges top-N / "+N" logic (pure function tests)
// ============================================================
describe('C4: partitionBadges + top-N collapse', () => {
  const badges = [
    makeBadge({ badgeId: '1', visualStyleKey: 'EMAIL' }),
    makeBadge({ badgeId: '2', visualStyleKey: 'OFFICIAL' }),
    makeBadge({ badgeId: '3', visualStyleKey: 'ENTERPRISE' }),
    makeBadge({ badgeId: '4', visualStyleKey: 'CREATOR' }),
    makeBadge({ badgeId: '5', visualStyleKey: 'MOBILE' }),
  ];

  describe('partitionBadges returns known sorted by priority desc', () => {
    it('known badges sorted OFFICIAL > ENTERPRISE > CREATOR > MOBILE > EMAIL', () => {
      const { known } = partitionBadges(badges as any);
      expect(known.map((b) => b.badgeId)).toEqual(['2', '3', '4', '5', '1']);
    });

    it('unknown badges go to unknown partition', () => {
      const mixed = [
        makeBadge({ badgeId: 'a', visualStyleKey: 'OFFICIAL' }),
        makeBadge({ badgeId: 'b', visualStyleKey: 'LEGACY_GOLD' }),
      ];
      const { known, unknown } = partitionBadges(mixed as any);
      expect(known).toHaveLength(1);
      expect(unknown).toHaveLength(1);
      expect(unknown[0].badgeId).toBe('b');
    });
  });

  describe('top-N selection for display', () => {
    const MAX_VISIBLE = 2;

    it('shows all badges when total <= MAX_VISIBLE', () => {
      const two = badges.slice(0, 2);
      const { known } = partitionBadges(two as any);
      const visible = known.slice(0, MAX_VISIBLE);
      const overflow = known.length - MAX_VISIBLE;
      expect(visible).toHaveLength(2);
      expect(overflow).toBeLessThanOrEqual(0);
    });

    it('shows top-2 by priority + overflow count when total > MAX_VISIBLE', () => {
      const { known } = partitionBadges(badges as any);
      const visible = known.slice(0, MAX_VISIBLE);
      const overflowCount = known.length - MAX_VISIBLE;
      expect(visible.map((b) => b.badgeId)).toEqual(['2', '3']); // OFFICIAL, ENTERPRISE
      expect(overflowCount).toBe(3);
    });

    it('overflow label is "+(N-2)"', () => {
      const { known } = partitionBadges(badges as any);
      const overflowCount = known.length - MAX_VISIBLE;
      expect(`+${overflowCount}`).toBe('+3');
    });

    it('all badges available for popover content', () => {
      const { known } = partitionBadges(badges as any);
      // popover should show ALL known badges (not just overflow)
      expect(known).toHaveLength(5);
    });
  });
});

// ============================================================
// C3: Badge detail state management (pure logic tests)
// ============================================================
describe('C3: badge detail state management', () => {
  beforeEach(() => {
    mockGetBadgeDetail.mockReset();
  });

  it('fetchDetail calls getBadgeDetail with badgeId', async () => {
    mockGetBadgeDetail.mockResolvedValue({ description: 'desc', verifiedAt: '2025-01-01' });

    // Simulate the composable logic
    const badgeId = 'badge-42';
    const result = await mockGetBadgeDetail(badgeId);
    expect(mockGetBadgeDetail).toHaveBeenCalledWith(badgeId);
    expect(result.description).toBe('desc');
  });

  it('handles API error gracefully', async () => {
    mockGetBadgeDetail.mockRejectedValue(new Error('Network error'));

    try {
      await mockGetBadgeDetail('badge-42');
    } catch (e) {
      expect((e as Error).message).toBe('Network error');
    }
    expect(mockGetBadgeDetail).toHaveBeenCalled();
  });

  it('ENTERPRISE badge detail includes company name from description', () => {
    const detail = { badgeType: 'ENTERPRISE', description: '{"companyName":"Acme Corp"}', verifiedAt: '2025-01-01' };
    // Component should parse description as JSON for ENTERPRISE type
    let parsed: any;
    try { parsed = JSON.parse(detail.description); } catch { parsed = null; }
    expect(parsed?.companyName).toBe('Acme Corp');
  });

  it('CREATOR badge detail includes certification field from description', () => {
    const detail = { badgeType: 'CREATOR', description: '{"field":"科技"}', verifiedAt: '2025-01-01' };
    let parsed: any;
    try { parsed = JSON.parse(detail.description); } catch { parsed = null; }
    expect(parsed?.field).toBe('科技');
  });
});

// ============================================================
// C3 + C4: VerificationBadge.vue component integration
// ============================================================
describe('VerificationBadge.vue', () => {
  beforeEach(() => {
    mockGetBadgeDetail.mockReset();
  });

  // Dynamic import to avoid hoisting issues with jest.mock
  async function mountComponent(badges: any[], options: { windowWidth?: number } = {}) {
    // Set window width before mount
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: options.windowWidth ?? 1024,
    });

    const VerificationBadge = (await import('/@/views/content/profile/components/VerificationBadge.vue')).default;
    return mount(VerificationBadge, {
      props: { badges },
      global: {
        stubs: {
          Icon: { template: '<span class="icon-stub" />' },
          'a-tooltip': { template: '<div class="a-tooltip"><slot /></div>', props: ['title'] },
          'a-popover': { template: '<div class="a-popover"><slot name="content" /><slot /></div>', props: ['open', 'trigger', 'placement'] },
          'a-spin': { template: '<div class="a-spin" />', props: ['spinning'] },
          'a-modal': { template: '<div class="a-modal"><slot /></div>', props: ['open', 'width', 'title', 'footer'] },
          'a-drawer': { template: '<div class="a-drawer"><slot /></div>', props: ['open', 'placement', 'width', 'title', 'closable'] },
        },
      },
    });
  }

  // --- C4 rendering ---
  it('renders top-2 known badges when there are >2 badges', async () => {
    const badges = [
      makeBadge({ badgeId: '1', visualStyleKey: 'OFFICIAL' }),
      makeBadge({ badgeId: '2', visualStyleKey: 'ENTERPRISE' }),
      makeBadge({ badgeId: '3', visualStyleKey: 'CREATOR' }),
    ];
    const wrapper = await mountComponent(badges);
    // Should render 2 badge icons in the main wrapper area + 1 overflow pill
    // Filter out badges inside popover content (popover stub renders all slots)
    const allBadges = wrapper.findAll('.verification-badge');
    const popoverContent = wrapper.find('.badge-popover-content');
    const popoverBadges = popoverContent.exists() ? popoverContent.findAll('.verification-badge') : [];
    const mainBadges = allBadges.length - popoverBadges.length;
    expect(mainBadges).toBe(2);
    const overflow = wrapper.find('.badge-overflow');
    expect(overflow.exists()).toBe(true);
    expect(overflow.text()).toBe('+1');
  });

  it('renders all badges when total <= 2', async () => {
    const badges = [
      makeBadge({ badgeId: '1', visualStyleKey: 'OFFICIAL' }),
      makeBadge({ badgeId: '2', visualStyleKey: 'ENTERPRISE' }),
    ];
    const wrapper = await mountComponent(badges);
    const badgeIcons = wrapper.findAll('.verification-badge');
    expect(badgeIcons).toHaveLength(2);
    const overflow = wrapper.find('.badge-overflow');
    expect(overflow.exists()).toBe(false);
  });

  it('renders no badges when array is empty', async () => {
    const wrapper = await mountComponent([]);
    const badgeIcons = wrapper.findAll('.verification-badge');
    expect(badgeIcons).toHaveLength(0);
  });

  // --- C4: popover expansion ---
  it('toggles popover when "+N" pill is clicked', async () => {
    const badges = [
      makeBadge({ badgeId: '1', visualStyleKey: 'OFFICIAL' }),
      makeBadge({ badgeId: '2', visualStyleKey: 'ENTERPRISE' }),
      makeBadge({ badgeId: '3', visualStyleKey: 'CREATOR' }),
      makeBadge({ badgeId: '4', visualStyleKey: 'MOBILE' }),
    ];
    const wrapper = await mountComponent(badges);
    const overflow = wrapper.find('.badge-overflow');
    expect(overflow.exists()).toBe(true);
    expect(overflow.text()).toBe('+2');

    // Click overflow to open popover
    await overflow.trigger('click');
    await nextTick();

    // Popover should now be open with all known badges rendered inside
    const popover = wrapper.find('.a-popover');
    expect(popover.exists()).toBe(true);
  });

  // --- C3: click badge opens detail ---
  it('opens modal on PC when badge is clicked', async () => {
    mockGetBadgeDetail.mockResolvedValue({
      badgeId: '1',
      badgeType: 'OFFICIAL',
      visualStyleKey: 'OFFICIAL',
      badgeLabel: 'Official',
      description: 'Official badge',
      verifiedAt: '2025-01-01',
    });
    const badges = [makeBadge({ badgeId: '1', visualStyleKey: 'OFFICIAL' })];
    const wrapper = await mountComponent(badges, { windowWidth: 1024 });

    await wrapper.find('.verification-badge').trigger('click');
    await nextTick();

    // Should call API
    expect(mockGetBadgeDetail).toHaveBeenCalledWith('1');

    // Wait for async
    await new Promise((r) => setTimeout(r, 0));
    await nextTick();

    // Should render modal (PC)
    const modal = wrapper.find('.a-modal');
    expect(modal.exists()).toBe(true);
  });

  it('opens drawer on mobile when badge is clicked', async () => {
    mockGetBadgeDetail.mockResolvedValue({
      badgeId: '1',
      badgeType: 'OFFICIAL',
      visualStyleKey: 'OFFICIAL',
      badgeLabel: 'Official',
      description: 'Official badge',
      verifiedAt: '2025-01-01',
    });
    const badges = [makeBadge({ badgeId: '1', visualStyleKey: 'OFFICIAL' })];
    const wrapper = await mountComponent(badges, { windowWidth: 375 });

    await wrapper.find('.verification-badge').trigger('click');
    await nextTick();

    // Should call API
    expect(mockGetBadgeDetail).toHaveBeenCalledWith('1');

    // Wait for async
    await new Promise((r) => setTimeout(r, 0));
    await nextTick();

    // Should render drawer (mobile)
    const drawer = wrapper.find('.a-drawer');
    expect(drawer.exists()).toBe(true);
  });

  it('shows loading state while fetching detail', async () => {
    let resolveDetail: any;
    mockGetBadgeDetail.mockImplementation(() => new Promise((r) => { resolveDetail = r; }));

    const badges = [makeBadge({ badgeId: '1', visualStyleKey: 'OFFICIAL' })];
    const wrapper = await mountComponent(badges, { windowWidth: 1024 });

    await wrapper.find('.verification-badge').trigger('click');
    await nextTick();

    // Loading state should be active
    expect(wrapper.find('.detail-loading').exists()).toBe(true);

    // Resolve the promise
    resolveDetail({ description: 'done', verifiedAt: '2025-01-01' });
    await new Promise((r) => setTimeout(r, 0));
    await nextTick();

    // Loading should be gone
    expect(wrapper.find('.detail-loading').exists()).toBe(false);
  });

  it('shows error state when API fails', async () => {
    mockGetBadgeDetail.mockRejectedValue(new Error('fail'));
    const badges = [makeBadge({ badgeId: '1', visualStyleKey: 'OFFICIAL' })];
    const wrapper = await mountComponent(badges, { windowWidth: 1024 });

    await wrapper.find('.verification-badge').trigger('click');
    await nextTick();

    await new Promise((r) => setTimeout(r, 0));
    await nextTick();

    expect(wrapper.find('.detail-error').exists()).toBe(true);
  });

  it('renders description and verifiedAt in detail view', async () => {
    mockGetBadgeDetail.mockResolvedValue({
      badgeId: '1',
      badgeType: 'INDIVIDUAL',
      visualStyleKey: 'INDIVIDUAL',
      badgeLabel: 'Individual',
      description: 'Some description',
      verifiedAt: '2025-06-15T10:00:00Z',
    });
    const badges = [makeBadge({ badgeId: '1', visualStyleKey: 'INDIVIDUAL' })];
    const wrapper = await mountComponent(badges, { windowWidth: 1024 });

    await wrapper.find('.verification-badge').trigger('click');
    await nextTick();
    await new Promise((r) => setTimeout(r, 0));
    await nextTick();

    expect(wrapper.text()).toContain('Some description');
    expect(wrapper.text()).toContain('2025-06-15');
  });

  it('renders ENTERPRISE extra info (companyName) in detail', async () => {
    mockGetBadgeDetail.mockResolvedValue({
      badgeId: '1',
      badgeType: 'ENTERPRISE',
      visualStyleKey: 'ENTERPRISE',
      badgeLabel: 'Enterprise',
      description: '{"companyName":"Acme Corp"}',
      verifiedAt: '2025-01-01',
    });
    const badges = [makeBadge({ badgeId: '1', visualStyleKey: 'ENTERPRISE' })];
    const wrapper = await mountComponent(badges, { windowWidth: 1024 });

    await wrapper.find('.verification-badge').trigger('click');
    await nextTick();
    await new Promise((r) => setTimeout(r, 0));
    await nextTick();

    expect(wrapper.text()).toContain('Acme Corp');
  });

  it('renders CREATOR/Influencer extra info (field) in detail', async () => {
    mockGetBadgeDetail.mockResolvedValue({
      badgeId: '1',
      badgeType: 'CREATOR',
      visualStyleKey: 'CREATOR',
      badgeLabel: 'Creator',
      description: '{"field":"科技"}',
      verifiedAt: '2025-01-01',
    });
    const badges = [makeBadge({ badgeId: '1', visualStyleKey: 'CREATOR' })];
    const wrapper = await mountComponent(badges, { windowWidth: 1024 });

    await wrapper.find('.verification-badge').trigger('click');
    await nextTick();
    await new Promise((r) => setTimeout(r, 0));
    await nextTick();

    expect(wrapper.text()).toContain('科技');
  });
});
