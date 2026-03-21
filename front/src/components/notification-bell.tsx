'use client';

import { useState } from 'react';
import Link from 'next/link';

import { Bell } from 'lucide-react';

import {
  useMarkAllAsRead,
  useMarkAsRead,
  useNotifications,
  useUnreadCount,
} from '@/hooks/use-notifications';
import { formatPrice } from '@/lib/format';

export function NotificationBell() {
  const [open, setOpen] = useState(false);
  const { data: unreadData } = useUnreadCount();
  const { data: notifications = [] } = useNotifications();
  const { mutate: markAsRead } = useMarkAsRead();
  const { mutate: markAllAsRead } = useMarkAllAsRead();

  const unreadCount = unreadData?.count ?? 0;

  const handleNotificationClick = (id: number, complexId: number | null) => {
    markAsRead(id);
    setOpen(false);
  };

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((prev) => !prev)}
        className="relative rounded-lg p-1.5 hover:bg-gray-100"
      >
        <Bell className="h-5 w-5 text-gray-600" />
        {unreadCount > 0 && (
          <span className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <>
          {/* 배경 클릭으로 닫기 */}
          <div className="fixed inset-0 z-10" onClick={() => setOpen(false)} />

          <div className="absolute right-0 top-9 z-20 w-80 rounded-xl border bg-white shadow-lg">
            {/* 헤더 */}
            <div className="flex items-center justify-between border-b px-4 py-3">
              <span className="text-sm font-semibold text-gray-900">알림</span>
              {unreadCount > 0 && (
                <button
                  onClick={() => markAllAsRead()}
                  className="text-xs text-blue-600 hover:underline"
                >
                  전체 읽음
                </button>
              )}
            </div>

            {/* 알림 목록 */}
            <div className="max-h-96 overflow-y-auto">
              {notifications.length === 0 ? (
                <div className="flex items-center justify-center py-10 text-sm text-gray-400">
                  알림이 없습니다
                </div>
              ) : (
                notifications.map((n) => (
                  <div
                    key={n.id}
                    onClick={() => handleNotificationClick(n.id, n.complexId)}
                    className={`cursor-pointer border-b px-4 py-3 last:border-0 hover:bg-gray-50 ${
                      !n.isRead ? 'bg-blue-50/50' : ''
                    }`}
                  >
                    {n.complexId ? (
                      <Link href={`/${n.complexId}`} onClick={(e) => e.stopPropagation()}>
                        <p className="text-sm font-medium text-gray-900">
                          {n.complexName ?? '단지'}{n.exclusivePyeong ? ` ${n.exclusivePyeong}평` : ''}
                        </p>
                        <p className="mt-0.5 text-xs text-gray-500">
                          새 거래 {n.price ? formatPrice(n.price) : ''}{' '}
                          {n.contractDate ?? ''}
                        </p>
                      </Link>
                    ) : (
                      <p className="text-sm text-gray-900">새로운 거래 알림</p>
                    )}
                    {!n.isRead && (
                      <span className="mt-1 inline-block h-1.5 w-1.5 rounded-full bg-blue-500" />
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
